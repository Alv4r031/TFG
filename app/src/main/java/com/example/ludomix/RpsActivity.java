package com.example.ludomix;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class RpsActivity extends AppCompatActivity {

    // 1. Constantes para hacer el código más legible
    private static final int ROCK = 0;
    private static final int PAPER = 1;
    private static final int SCISSORS = 2;

    private TextView resultTextView;
    private TextView scoreTextView; // Opcional: para llevar la puntuación
    private TextView playerChoiceView;
    private TextView cpuChoiceView;

    private int playerScore = 0;
    private int cpuScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rps);

        int resTxtResult = getResources().getIdentifier("txtResult", "id", getPackageName());
        int resTxtScore = getResources().getIdentifier("txtScore", "id", getPackageName());
        int resBtnBack = getResources().getIdentifier("btnBackToMenu", "id", getPackageName());

        if (resTxtResult != 0) resultTextView = findViewById(resTxtResult);
        if (resTxtScore != 0) scoreTextView = findViewById(resTxtScore);

        playerChoiceView = findViewById(R.id.playerChoiceView);
        cpuChoiceView = findViewById(R.id.cpuChoiceView);

        if (resBtnBack != 0) {
            Button btnBack = findViewById(resBtnBack);
            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        }

        updateScoreText();
        if (resultTextView != null) resultTextView.setText(getString(R.string.rps_start_prompt));
    }

    // 2. Métodos para ser llamados desde los botones en el XML
    public void playRock(View view) {
        play(ROCK);
    }

    public void playPaper(View view) {
        play(PAPER);
    }

    public void playScissors(View view) {
        play(SCISSORS);
    }

    private void play(int playerChoice) {
        int cpuChoice = new Random().nextInt(3); // Genera 0, 1 o 2

        // Mostrar emojis en las vistas grandes
        if (playerChoiceView != null) playerChoiceView.setText(choiceToEmoji(playerChoice));
        if (cpuChoiceView != null) cpuChoiceView.setText(choiceToEmoji(cpuChoice));

        String resultText;

        // 3. Lógica completa del juego
        if (playerChoice == cpuChoice) {
            // Empate
            resultText = getString(R.string.rps_tie, choiceToString(playerChoice));
        } else if ((playerChoice == ROCK && cpuChoice == SCISSORS) ||
                (playerChoice == PAPER && cpuChoice == ROCK) ||
                (playerChoice == SCISSORS && cpuChoice == PAPER)) {
            // El jugador gana
            playerScore++;
            resultText = getString(R.string.rps_win, choiceToString(playerChoice), choiceToString(cpuChoice));
        } else {
            // La CPU gana
            cpuScore++;
            resultText = getString(R.string.rps_lose, choiceToString(cpuChoice), choiceToString(playerChoice));
        }

        if (resultTextView != null) resultTextView.setText(resultText);
        updateScoreText();
    }

    /**
     * Convierte la elección numérica a una cadena de texto legible.
     */
    private String choiceToString(int choice) {
        switch (choice) {
            case ROCK:
                return getString(R.string.rock);
            case PAPER:
                return getString(R.string.paper);
            case SCISSORS:
                return getString(R.string.scissors);
            default:
                return "";
        }
    }

    private String choiceToEmoji(int choice) {
        switch (choice) {
            case ROCK:
                return "✊";
            case PAPER:
                return "📄";
            case SCISSORS:
                return "✂️";
            default:
                return "";
        }
    }

    /**
     * Actualiza el TextView de la puntuación.
     */
    private void updateScoreText() {
        if (scoreTextView != null) scoreTextView.setText(getString(R.string.score_text, playerScore, cpuScore));
    }

    /**
     * Reinicia el marcador del juego.
     * Puedes vincularlo a un botón "Reiniciar" en tu XML.
     */
    public void resetGame(View view) {
        playerScore = 0;
        cpuScore = 0;
        updateScoreText();
        if (resultTextView != null) resultTextView.setText(getString(R.string.rps_start_prompt));
        if (playerChoiceView != null) playerChoiceView.setText("");
        if (cpuChoiceView != null) cpuChoiceView.setText("");
    }
}
