package com.example.ludomix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class GuessNumberActivity extends AppCompatActivity {

    private int secretNumber;
    private TextView txtResult;
    private TextView txtAttempts;
    private EditText txtGuess;
    private Button btnCheck;
    private Button btnPlayAgain;

    private int attempts = 0; // contador de intentos
    private int difficulty = GuessNumberDifficultyActivity.DIFF_EASY;
    private int maxNumber = 50;
    private String difficultyName = "Fácil";

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable clearHintRunnable;

    private UsuarioDAO usuarioDAO;
    private PuntuacionDAO puntuacionDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        puntuacionDAO = new PuntuacionDAO(this);
        puntuacionDAO.open();

        // Obtener dificultad del Intent
        if (getIntent() != null && getIntent().hasExtra(GuessNumberDifficultyActivity.EXTRA_DIFFICULTY)) {
            difficulty = getIntent().getIntExtra(GuessNumberDifficultyActivity.EXTRA_DIFFICULTY, GuessNumberDifficultyActivity.DIFF_EASY);
            
            switch (difficulty) {
                case GuessNumberDifficultyActivity.DIFF_EASY:
                    maxNumber = 50;
                    difficultyName = "Fácil";
                    break;
                case GuessNumberDifficultyActivity.DIFF_MEDIUM:
                    maxNumber = 100;
                    difficultyName = "Medio";
                    break;
                case GuessNumberDifficultyActivity.DIFF_HARD:
                    maxNumber = 500;
                    difficultyName = "Difícil";
                    break;
            }
        }

        // findViewById en lugar de ViewBinding
        txtResult = findViewById(R.id.txtResult);
        txtAttempts = findViewById(R.id.txtAttempts);
        txtGuess = findViewById(R.id.txtGuess);
        btnCheck = findViewById(R.id.btnCheck);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);

        // Botón volver
        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Asigna el listener al botón de comprobar
        btnCheck.setOnClickListener(v -> checkGuess());

        // Asigna el listener al botón de reiniciar
        btnPlayAgain.setOnClickListener(v -> playAgain());

        startNewGame();
    }

    private void startNewGame() {
        secretNumber = new Random().nextInt(maxNumber) + 1;
        attempts = 0; // resetear contador
        txtResult.setText(getString(R.string.guess_a_number_prompt));
        if (txtAttempts != null) {
            txtAttempts.setText("Turnos: 0");
        }
        txtGuess.setText("");
        txtGuess.setEnabled(true);
        btnCheck.setEnabled(true);
    }

    private void checkGuess() {
        String guessString = txtGuess.getText().toString();

        if (guessString.isEmpty()) {
            txtResult.setText(getString(R.string.empty_guess_error));
            return;
        }

        try {
            int guess = Integer.parseInt(guessString);

            // Contabilizar intento válido
            attempts++;
            if (txtAttempts != null) {
                txtAttempts.setText("Turnos: " + attempts);
            }

            if (guess == secretNumber) {
                // Mostrar mensaje con número de turnos
                String msg = getString(R.string.correct_guess_in_turns, attempts);
                if (clearHintRunnable != null) {
                    handler.removeCallbacks(clearHintRunnable);
                }
                txtResult.setText(msg);
                txtGuess.setEnabled(false);
                btnCheck.setEnabled(false);

                // Actualizar estadísticas locales: plays +1 y wins +1
                int plays = prefs.getInt("plays_guess", 0);
                int wins = prefs.getInt("wins_guess", 0);
                prefs.edit().putInt("plays_guess", plays + 1).putInt("wins_guess", wins + 1).apply();

                String juegoName = "Adivina Número - " + difficultyName;
                guardarPuntuacion(juegoName, Math.max(100 - attempts * 5, 10));

            } else if (guess < secretNumber) {
                txtResult.setText(getString(R.string.guess_too_low));
                scheduleClearHint();
                txtGuess.setText("");

                // No incrementar plays aquí: todavía no ha terminado la partida

            } else {
                txtResult.setText(getString(R.string.guess_too_high));
                scheduleClearHint();
                txtGuess.setText("");

                // No incrementar plays aquí: todavía no ha terminado la partida
            }
        } catch (NumberFormatException e) {
            txtResult.setText(getString(R.string.invalid_number_error));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
        if (puntuacionDAO != null) {
            puntuacionDAO.close();
        }
    }

    private void guardarPuntuacion(String juego, int puntos) {
        String username = prefs.getString(KEY_LOGGED_IN, null);
        if (username == null) return;

        Puntuacion puntuacion = new Puntuacion(username, puntos, juego);
        if (puntuacionDAO.registrarPuntuacion(puntuacion)) {
            Usuario usuario = usuarioDAO.obtenerUsuario(username);
            if (usuario != null) {
                usuarioDAO.actualizarPuntuacion(username, usuario.getPuntuacion() + puntos);
            }
            Toast.makeText(this, "+" + puntos + " puntos guardados", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleClearHint() {
        if (clearHintRunnable != null) {
            handler.removeCallbacks(clearHintRunnable);
        }
        clearHintRunnable = () -> {
            if (txtResult != null && btnCheck.isEnabled()) {
                txtResult.setText(R.string.guess_a_number_prompt);
            }
        };
        handler.postDelayed(clearHintRunnable, 2000);
    }

    private void playAgain() {
        if (clearHintRunnable != null) {
            handler.removeCallbacks(clearHintRunnable);
        }
        startNewGame();
    }
}
