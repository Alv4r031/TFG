package com.example.ludomix;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToeActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_MODE = "EXTRA_GAME_MODE";
    public static final String EXTRA_HUMAN_IS_X = "EXTRA_HUMAN_IS_X";

    private Button[] buttons = new Button[9];
    private boolean playerXTurn = true; // True = X, False = O
    private int turnCount = 0;
    private TextView txtStatus;

    // Mode handling
    private String gameMode = "PVP"; // "PVP" or "PVC"
    private boolean humanIsX = true; // meaningful only if PVC

    private Random random = new Random();
    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        txtStatus = findViewById(R.id.txtStatus);

        for (int i = 0; i < buttons.length; i++) {
            String buttonID = "btn" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
        }

        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Read intent extras
        if (getIntent() != null && getIntent().getStringExtra(EXTRA_GAME_MODE) != null) {
            gameMode = getIntent().getStringExtra(EXTRA_GAME_MODE);
            if ("PVC".equals(gameMode)) {
                humanIsX = getIntent().getBooleanExtra(EXTRA_HUMAN_IS_X, true);
            }
        }

        // Restore state if needed
        if (savedInstanceState != null) {
            gameMode = savedInstanceState.getString(EXTRA_GAME_MODE, gameMode);
            humanIsX = savedInstanceState.getBoolean(EXTRA_HUMAN_IS_X, humanIsX);
            playerXTurn = savedInstanceState.getBoolean("playerXTurn", playerXTurn);
            turnCount = savedInstanceState.getInt("turnCount", turnCount);
        }

        // Initialize status text
        if (gameMode.equals("PVC")) {
            // Ensure X always starts
            playerXTurn = true;
            txtStatus.setText(R.string.turn_player_x);

            // If CPU is X (humanIsX == false) then CPU must play first
            if (!humanIsX && turnCount == 0) {
                cpuMakeMove();
            }
        } else {
            // PvP default behavior
            playerXTurn = true;
            txtStatus.setText(R.string.turn_player_x);
        }
    }

    public void onCellClick(View v) {
        Button button = (Button) v;

        // Si la celda ya está ocupada o el juego terminó, no hacer nada
        if (!button.getText().toString().equals("")) {
            return;
        }

        // En modo PVC, si no es el turno del humano, ignorar clicks
        if (gameMode.equals("PVC")) {
            boolean humanTurn = (playerXTurn && humanIsX) || (!playerXTurn && !humanIsX);
            if (!humanTurn) return; // No es turno humano
        }

        if (playerXTurn) {
            button.setText("X");
            button.setTextColor(getResources().getColor(R.color.playerX));
            txtStatus.setText(R.string.turn_player_o);
        } else {
            button.setText("O");
            button.setTextColor(getResources().getColor(R.color.playerO));
            txtStatus.setText(R.string.turn_player_x);
        }

        turnCount++;

        if (checkForWin()) {
            if (playerXTurn) {
                endGame(getString(R.string.player_x_wins));
            } else {
                endGame(getString(R.string.player_o_wins));
            }
        } else if (turnCount == 9) {
            endGame(getString(R.string.game_draw));
        } else {
            playerXTurn = !playerXTurn; // Cambiar turno

            // Si está en modo PVC y ahora es turno de la CPU, hacer jugada de la CPU
            if (gameMode.equals("PVC")) {
                boolean cpuTurn = (playerXTurn && !humanIsX) || (!playerXTurn && humanIsX);
                if (cpuTurn) {
                    cpuMakeMove();
                }
            }
        }
    }

    private boolean checkForWin() {
        String[][] field = new String[3][3];
        for (int i = 0; i < buttons.length; i++) {
            field[i / 3][i % 3] = buttons[i].getText().toString();
        }

        // Revisar filas y columnas
        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1]) && field[i][0].equals(field[i][2]) && !field[i][0].equals("")) {
                return true;
            }
            if (field[0][i].equals(field[1][i]) && field[0][i].equals(field[2][i]) && !field[0][i].equals("")) {
                return true;
            }
        }

        // Revisar diagonales
        if (field[0][0].equals(field[1][1]) && field[0][0].equals(field[2][2]) && !field[0][0].equals("")) {
            return true;
        }
        if (field[0][2].equals(field[1][1]) && field[0][2].equals(field[2][0]) && !field[0][2].equals("")) {
            return true;
        }

        return false;
    }

    private void endGame(String message) {
        txtStatus.setText(message);
        for (Button button : buttons) {
            button.setEnabled(false); // Deshabilitar tablero
        }

        // Actualizar estadísticas: contar la partida jugada
        try {
            int plays = prefs.getInt("plays_ttt", 0);
            prefs.edit().putInt("plays_ttt", plays + 1).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Determinar si hay ganador y si el humano (en PVC) es el ganador -> aumentar wins
        if ("PVC".equals(gameMode)) {
            String winnerText = message;
            boolean humanWon = false;
            if (winnerText != null) {
                if (winnerText.contains("X") && humanIsX) humanWon = true;
                if (winnerText.contains("O") && !humanIsX) humanWon = true;
            }
            if (humanWon) {
                try {
                    int wins = prefs.getInt("wins_ttt", 0);
                    prefs.edit().putInt("wins_ttt", wins + 1).apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void resetGame(View view) {
        playerXTurn = true;
        turnCount = 0;
        txtStatus.setText(R.string.turn_player_x);
        for (Button button : buttons) {
            button.setText("");
            button.setEnabled(true);
        }

        // If PVC and CPU is X, CPU starts again
        if (gameMode.equals("PVC")) {
            if (!humanIsX) {
                cpuMakeMove();
            }
        }
    }

    private void cpuMakeMove() {
        // Movimiento sencillo: elegir una celda vacía al azar
        List<Integer> emptyIndexes = new ArrayList<>();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getText().toString().equals("")) {
                emptyIndexes.add(i);
            }
        }
        if (emptyIndexes.isEmpty()) return;

        int choice = emptyIndexes.get(random.nextInt(emptyIndexes.size()));
        Button button = buttons[choice];

        // CPU juega la pieza correspondiente al turno actual
        if (playerXTurn) {
            button.setText("X");
            button.setTextColor(getResources().getColor(R.color.playerX));
            txtStatus.setText(R.string.turn_player_o);
        } else {
            button.setText("O");
            button.setTextColor(getResources().getColor(R.color.playerO));
            txtStatus.setText(R.string.turn_player_x);
        }

        turnCount++;

        if (checkForWin()) {
            if (playerXTurn) {
                endGame(getString(R.string.player_x_wins));
            } else {
                endGame(getString(R.string.player_o_wins));
            }
        } else if (turnCount == 9) {
            endGame(getString(R.string.game_draw));
        } else {
            playerXTurn = !playerXTurn; // Cambiar turno
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_GAME_MODE, gameMode);
        outState.putBoolean(EXTRA_HUMAN_IS_X, humanIsX);
        outState.putBoolean("playerXTurn", playerXTurn);
        outState.putInt("turnCount", turnCount);
    }
}
