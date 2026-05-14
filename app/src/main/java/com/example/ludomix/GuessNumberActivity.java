package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
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
    private TextView txtGuessProgress;
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
        txtGuessProgress = findViewById(R.id.txtGuessProgress);
        txtGuess = findViewById(R.id.txtGuess);
        btnCheck = findViewById(R.id.btnCheck);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);

        // Botón volver
        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        Button btnBackToDifficulty = findViewById(R.id.btnBackToDifficulty);
        if (btnBackToDifficulty != null) {
            btnBackToDifficulty.setOnClickListener(v -> {
                // Volver al selector de dificultad de Adivina Número
                Intent intent = new Intent(this, GuessNumberDifficultyActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Asigna el listener al botón de comprobar
        btnCheck.setOnClickListener(v -> checkGuess());

        // Asigna el listener al botón de reiniciar
        btnPlayAgain.setOnClickListener(v -> playAgain());

        startNewGame();
        updateGuessProgress();
    }

    private void startNewGame() {
        secretNumber = new Random().nextInt(maxNumber) + 1;
        attempts = 0; // resetear contador
        txtResult.setText(getString(R.string.guess_a_number_prompt, maxNumber));
        if (txtAttempts != null) {
            txtAttempts.setText("Turnos: 0");
        }
        updateGuessProgress();
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
                // Calcular puntos según dificultad y número de intentos
                int puntos;
                if (difficulty == GuessNumberDifficultyActivity.DIFF_EASY) {
                    // Reglas para nivel Fácil (ya definidas)
                    if (attempts < 5) {
                        puntos = 100;
                    } else if (attempts < 10) {
                        puntos = 75;
                    } else if (attempts < 15) {
                        puntos = 50;
                    } else {
                        puntos = 25;
                    }
                } else {
                    // Nuevo sistema solicitado para MEDIO y DIFÍCIL: 200/100/75/50 según intentos
                    if (attempts < 5) {
                        puntos = 200;
                    } else if (attempts < 10) {
                        puntos = 100;
                    } else if (attempts < 15) {
                        puntos = 75;
                    } else {
                        puntos = 50;
                    }
                }

                // Antes de guardar, comprobar puntos totales actuales para detectar desbloqueo
                int antesTotal = 0;
                String username = prefs.getString(KEY_LOGGED_IN, null);
                if (username != null) {
                    if (puntuacionDAO == null) {
                        puntuacionDAO = new PuntuacionDAO(this);
                        puntuacionDAO.open();
                    } else if (puntuacionDAO != null) {
                        try {
                            // Ensure DB open
                            puntuacionDAO.open();
                        } catch (Exception ignored) {}
                    }

                    android.database.Cursor cursor = puntuacionDAO.obtenerPuntuacionesUsuario(username);
                    String expectedJuego = "Adivina Número - " + difficultyName; // usar mismo formato que updateGuessProgress
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            String juego = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_JUEGO));
                            int pts = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS));
                            if (juego != null && juego.trim().equals(expectedJuego)) {
                                antesTotal += pts;
                            }
                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                }

                // Guardar la puntuación usando el DAO de instancia
                guardarPuntuacion(juegoName, puntos);

                // Después de guardar, comprobar si se ha cruzado el umbral
                int despuesTotal = antesTotal + puntos;
                if (difficulty == GuessNumberDifficultyActivity.DIFF_EASY && antesTotal < 500 && despuesTotal >= 500) {
                    Toast.makeText(this, "¡Has desbloqueado el nivel Medio!", Toast.LENGTH_LONG).show();
                } else if (difficulty == GuessNumberDifficultyActivity.DIFF_MEDIUM && antesTotal < 1000 && despuesTotal >= 1000) {
                    Toast.makeText(this, "¡Has desbloqueado el nivel Difícil!", Toast.LENGTH_LONG).show();
                }

                updateGuessProgress();

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
        if (username == null) {
            // Informar que no se guardó por falta de sesión
            Toast.makeText(this, "No has iniciado sesión: puntuación no guardada", Toast.LENGTH_SHORT).show();
            return;
        }

        // Asegurarnos de que el DAO esté abierto
        if (puntuacionDAO == null) {
            puntuacionDAO = new PuntuacionDAO(this);
            puntuacionDAO.open();
        }

        // Calcular total antes de insertar para comprobar hitos
        int antesTotal = 0;
        android.database.Cursor cursorBefore = puntuacionDAO.obtenerPuntuacionesUsuario(username);
        if (cursorBefore != null && cursorBefore.moveToFirst()) {
            do {
                antesTotal += cursorBefore.getInt(cursorBefore.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS));
            } while (cursorBefore.moveToNext());
            cursorBefore.close();
        }

        Puntuacion puntuacion = new Puntuacion(username, puntos, juego);
        boolean ok = false;
        try {
            ok = puntuacionDAO.registrarPuntuacion(puntuacion);
        } catch (Exception e) {
            // no hacer crash, mostrar mensaje
            Toast.makeText(this, "Error guardando puntuación", Toast.LENGTH_SHORT).show();
        }

        if (ok) {
            // Actualizar la puntuación acumulada en la tabla usuarios
            Usuario usuario = usuarioDAO.obtenerUsuario(username);
            if (usuario != null) {
                usuarioDAO.actualizarPuntuacion(username, usuario.getPuntuacion() + puntos);
            }
            Toast.makeText(this, "+" + puntos + " puntos guardados", Toast.LENGTH_SHORT).show();
            // Comprobar si se alcanzaron hitos de puntos totales
            int despuesTotal = antesTotal + puntos;
            ScoreMilestones.checkMilestones(this, username, antesTotal, despuesTotal);
            updateGuessProgress();
        } else {
            Toast.makeText(this, "No se pudo guardar la puntuación", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleClearHint() {
        if (clearHintRunnable != null) {
            handler.removeCallbacks(clearHintRunnable);
        }
        clearHintRunnable = () -> {
            if (txtResult != null && btnCheck.isEnabled()) {
                txtResult.setText(getString(R.string.guess_a_number_prompt, maxNumber));
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

    private void updateGuessProgress() {
        if (txtGuessProgress == null) return;
        String username = prefs.getString(KEY_LOGGED_IN, null);
        if (username == null) {
            txtGuessProgress.setText("Inicia sesión para ver progreso");
            return;
        }

        // Sumar puntos de Adivina Número usando el DAO de instancia
        if (puntuacionDAO == null) {
            puntuacionDAO = new PuntuacionDAO(this);
            puntuacionDAO.open();
        }

        int total = 0;
        android.database.Cursor cursor = puntuacionDAO.obtenerPuntuacionesUsuario(username);
        String expectedJuego = "Adivina Número - " + difficultyName; // sumar solo para la dificultad actual
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String juego = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_JUEGO));
                if (juego != null && juego.trim().equals(expectedJuego)) {
                    total += cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        int siguienteUmbral = Integer.MAX_VALUE;
        if (difficulty == GuessNumberDifficultyActivity.DIFF_EASY) siguienteUmbral = 500;
        else if (difficulty == GuessNumberDifficultyActivity.DIFF_MEDIUM) siguienteUmbral = 1000;
        else siguienteUmbral = total; // si difícil

        // Mostrar en formato sencillo solicitado
        txtGuessProgress.setText("Puntos para siguiente nivel: " + total + " / " + siguienteUmbral);
    }
}
