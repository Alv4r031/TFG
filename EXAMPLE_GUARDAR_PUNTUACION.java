package com.example.ludomix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

/**
 * EJEMPLO: Cómo integrar la base de datos SQLite en una actividad de juego
 * Este es un ejemplo basado en GuessNumberActivity
 * 
 * Cambios principales:
 * 1. Agregar UsuarioDAO y PuntuacionDAO
 * 2. En onDestroy, cerrar los DAOs
 * 3. Cuando el jugador gana, registrar la puntuación en la BD
 */
public class GuessNumberActivityExample extends AppCompatActivity {

    private int secretNumber;
    private TextView txtResult;
    private EditText txtGuess;
    private Button btnCheck;
    private Button btnPlayAgain;

    private int attempts = 0;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";
    
    // Nuevas referencias para BD
    private UsuarioDAO usuarioDAO;
    private PuntuacionDAO puntuacionDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        
        // Inicializar DAOs
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        
        puntuacionDAO = new PuntuacionDAO(this);
        puntuacionDAO.open();

        txtResult = findViewById(R.id.txtResult);
        txtGuess = findViewById(R.id.txtGuess);
        btnCheck = findViewById(R.id.btnCheck);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);

        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnCheck.setOnClickListener(v -> checkGuess());
        btnPlayAgain.setOnClickListener(v -> playAgain());

        startNewGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // IMPORTANTE: Cerrar los DAOs
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
        if (puntuacionDAO != null) {
            puntuacionDAO.close();
        }
    }

    private void startNewGame() {
        secretNumber = new Random().nextInt(100) + 1;
        attempts = 0;
        txtResult.setText(getString(R.string.guess_a_number_prompt));
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
            attempts++;

            if (guess == secretNumber) {
                String msg = getString(R.string.correct_guess_in_turns, attempts);
                txtResult.setText(msg);
                txtGuess.setEnabled(false);
                btnCheck.setEnabled(false);

                // NUEVO: Guardar puntuación en la BD
                String username = prefs.getString(KEY_LOGGED_IN, null);
                if (username != null) {
                    // Crear puntuación (basada en intentos: menos intentos = más puntos)
                    int puntos = Math.max(100 - attempts * 5, 10);
                    Puntuacion puntuacion = new Puntuacion(username, puntos, "Adivina Número");
                    
                    // Registrar en la BD
                    puntuacionDAO.registrarPuntuacion(puntuacion);
                    
                    // Actualizar puntuación total del usuario
                    int puntosActuales = usuarioDAO.obtenerUsuario(username).getPuntuacion();
                    usuarioDAO.actualizarPuntuacion(username, puntosActuales + puntos);
                }

                // Mantener estadísticas locales (opcional, se puede eliminar)
                int plays = prefs.getInt("plays_guess", 0);
                int wins = prefs.getInt("wins_guess", 0);
                prefs.edit()
                    .putInt("plays_guess", plays + 1)
                    .putInt("wins_guess", wins + 1)
                    .apply();

            } else if (guess < secretNumber) {
                txtResult.setText(getString(R.string.guess_too_low));
            } else {
                txtResult.setText(getString(R.string.guess_too_high));
            }
        } catch (NumberFormatException e) {
            txtResult.setText(getString(R.string.invalid_number_error));
        }
    }

    private void playAgain() {
        startNewGame();
    }
}

