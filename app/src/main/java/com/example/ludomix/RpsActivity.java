package com.example.ludomix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

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
    private TextView playerWinsView;
    private TextView cpuWinsView;

    private int playerScore = 0;
    private int cpuScore = 0;
    private int playerWins = 0;
    private int cpuWins = 0;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";

    private UsuarioDAO usuarioDAO;
    private PuntuacionDAO puntuacionDAO;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable clearChoicesRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rps);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        puntuacionDAO = new PuntuacionDAO(this);
        puntuacionDAO.open();

        int resTxtResult = getResources().getIdentifier("txtResult", "id", getPackageName());
        int resTxtScore = getResources().getIdentifier("txtScore", "id", getPackageName());
        int resBtnBack = getResources().getIdentifier("btnBackToMenu", "id", getPackageName());

        if (resTxtResult != 0) resultTextView = findViewById(resTxtResult);
        if (resTxtScore != 0) scoreTextView = findViewById(resTxtScore);

        playerChoiceView = findViewById(R.id.playerChoiceView);
        cpuChoiceView = findViewById(R.id.cpuChoiceView);
        playerWinsView = findViewById(R.id.playerWinsView);
        cpuWinsView = findViewById(R.id.cpuWinsView);

        if (resBtnBack != 0) {
            Button btnBack = findViewById(resBtnBack);
            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        }

        updateScoreText();
        if (resultTextView != null) resultTextView.setText(getString(R.string.rps_start_prompt));
        updateWinsViews();
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
        boolean playerWon = false;
        if (playerChoice == cpuChoice) {
            // Empate
            resultText = getString(R.string.rps_tie, choiceToString(playerChoice));
        } else if ((playerChoice == ROCK && cpuChoice == SCISSORS) ||
                (playerChoice == PAPER && cpuChoice == ROCK) ||
                (playerChoice == SCISSORS && cpuChoice == PAPER)) {
            // El jugador gana
            playerScore++;
            playerWins++;
            playerWon = true;
            resultText = getString(R.string.rps_win, choiceToString(playerChoice), choiceToString(cpuChoice));
        } else {
            // La CPU gana
            cpuScore++;
            cpuWins++;
            resultText = getString(R.string.rps_lose, choiceToString(cpuChoice), choiceToString(playerChoice));
        }

        if (resultTextView != null) resultTextView.setText(resultText);
        updateScoreText();
        updateWinsViews();
        scheduleClearChoices();

        // Guardar estadísticas locales
        try {
            int plays = prefs.getInt("plays_rps", 0);
            int wins = prefs.getInt("wins_rps", 0);
            plays = plays + 1;
            if (playerWon) wins = wins + 1;
            prefs.edit().putInt("plays_rps", plays).putInt("wins_rps", wins).apply();

            if (playerWon) {
                guardarPuntuacion("Piedra Papel Tijera", 10);
            }
            // Feedback al usuario
            Toast.makeText(this, "Estadísticas actualizadas", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
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

        // Calcular total antes de insertar para comprobar hitos
        int antesTotal = 0;
        if (puntuacionDAO == null) {
            puntuacionDAO = new PuntuacionDAO(this);
            puntuacionDAO.open();
        }
        android.database.Cursor cursorBefore = puntuacionDAO.obtenerPuntuacionesUsuario(username);
        if (cursorBefore != null && cursorBefore.moveToFirst()) {
            do {
                antesTotal += cursorBefore.getInt(cursorBefore.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS));
            } while (cursorBefore.moveToNext());
            cursorBefore.close();
        }

        Puntuacion puntuacion = new Puntuacion(username, puntos, juego);
        if (puntuacionDAO.registrarPuntuacion(puntuacion)) {
            Usuario usuario = usuarioDAO.obtenerUsuario(username);
            if (usuario != null) {
                usuarioDAO.actualizarPuntuacion(username, usuario.getPuntuacion() + puntos);
            }
            // Comprobar hitos
            int despuesTotal = antesTotal + puntos;
            ScoreMilestones.checkMilestones(this, username, antesTotal, despuesTotal);
        }
    }

    private void scheduleClearChoices() {
        if (clearChoicesRunnable != null) {
            handler.removeCallbacks(clearChoicesRunnable);
        }
        clearChoicesRunnable = () -> {
            if (playerChoiceView != null) playerChoiceView.setText("");
            if (cpuChoiceView != null) cpuChoiceView.setText("");
        };
        // Aumentamos ligeramente el delay para que los iconos tarden un poco más en desaparecer
        handler.postDelayed(clearChoicesRunnable, 1500);
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

    private void updateWinsViews() {
        if (playerWinsView != null) playerWinsView.setText("Tú: " + playerWins);
        if (cpuWinsView != null) cpuWinsView.setText("CPU: " + cpuWins);
    }

    /**
     * Reinicia el marcador del juego.
     * Puedes vincularlo a un botón "Reiniciar" en tu XML.
     */
    public void resetGame(View view) {
        if (clearChoicesRunnable != null) {
            handler.removeCallbacks(clearChoicesRunnable);
        }
        playerScore = 0;
        cpuScore = 0;
        playerWins = 0;
        cpuWins = 0;
        updateScoreText();
        updateWinsViews();
        if (resultTextView != null) resultTextView.setText(getString(R.string.rps_start_prompt));
        if (playerChoiceView != null) playerChoiceView.setText("");
        if (cpuChoiceView != null) cpuChoiceView.setText("");
    }
}
