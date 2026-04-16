package com.example.ludomix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class GuessNumberActivity extends AppCompatActivity {

    private int secretNumber;
    private TextView txtResult;
    private EditText txtGuess;
    private Button btnCheck;
    private Button btnPlayAgain;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // findViewById en lugar de ViewBinding
        txtResult = findViewById(R.id.txtResult);
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
        secretNumber = new Random().nextInt(100) + 1;
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

            if (guess == secretNumber) {
                txtResult.setText(getString(R.string.correct_guess));
                txtGuess.setEnabled(false);
                btnCheck.setEnabled(false);

                // Actualizar estadísticas locales: plays +1 y wins +1
                int plays = prefs.getInt("plays_guess", 0);
                int wins = prefs.getInt("wins_guess", 0);
                prefs.edit().putInt("plays_guess", plays + 1).putInt("wins_guess", wins + 1).apply();
                // Feedback al usuario
                Toast.makeText(this, "Estadísticas actualizadas", Toast.LENGTH_SHORT).show();

            } else if (guess < secretNumber) {
                txtResult.setText(getString(R.string.guess_too_low));

                // No incrementar plays aquí: todavía no ha terminado la partida

            } else {
                txtResult.setText(getString(R.string.guess_too_high));

                // No incrementar plays aquí: todavía no ha terminado la partida
            }
        } catch (NumberFormatException e) {
            txtResult.setText(getString(R.string.invalid_number_error));
        }
    }

    private void playAgain() {
        startNewGame();
    }
}
