package com.example.ludomix;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class GuessNumberActivity extends AppCompatActivity {

    private int secretNumber;
    private TextView txtResult;
    private EditText txtGuess;
    private Button btnCheck;
    private Button btnPlayAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);

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
