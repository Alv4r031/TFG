package com.example.ludomix;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.os.SystemClock;

public class TicTacToeActivity extends AppCompatActivity {

    private static final String TAG = "TicTacToeActivity";

    public static final String EXTRA_GAME_MODE = "EXTRA_GAME_MODE";
    public static final String EXTRA_HUMAN_IS_X = "EXTRA_HUMAN_IS_X";
    public static final String EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY"; // nuevo extra

    private Button[] buttons = new Button[9];
    private boolean playerXTurn = true; // True = X, False = O
    private int turnCount = 0;
    private TextView txtStatus;
    private TextView overlayIcon; // nuevo overlay

    // Mode handling
    private String gameMode = "PVP"; // "PVP" or "PVC"
    private boolean humanIsX = true; // meaningful only if PVC

    // Dificultad de IA (si aplica)
    private int tttDifficulty = 0; // 0=Fácil,1=Medio,2=Difícil

    private Random random = new Random();
    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";

    private UsuarioDAO usuarioDAO;
    private PuntuacionDAO puntuacionDAO;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideOverlayRunnable; // runnable cancelable para ocultar overlay
    private long overlayShownAt = 0L; // timestamp de la última vez se mostró overlay (uptimeMillis)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        puntuacionDAO = new PuntuacionDAO(this);
        puntuacionDAO.open();

        txtStatus = findViewById(R.id.txtStatus);
        overlayIcon = findViewById(R.id.overlayIcon);

        for (int i = 0; i < buttons.length; i++) {
            String buttonID = "btn" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
        }

        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Read intent extras
        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_GAME_MODE) != null) {
                gameMode = getIntent().getStringExtra(EXTRA_GAME_MODE);
                if ("PVC".equals(gameMode)) {
                    humanIsX = getIntent().getBooleanExtra(EXTRA_HUMAN_IS_X, true);
                }
            }
            // Leer dificultad enviada por TicTacToeDifficultyActivity (si existe)
            tttDifficulty = getIntent().getIntExtra(EXTRA_DIFFICULTY, 0);
        }

        // Restore state if needed
        if (savedInstanceState != null) {
            gameMode = savedInstanceState.getString(EXTRA_GAME_MODE, gameMode);
            humanIsX = savedInstanceState.getBoolean(EXTRA_HUMAN_IS_X, humanIsX);
            playerXTurn = savedInstanceState.getBoolean("playerXTurn", playerXTurn);
            turnCount = savedInstanceState.getInt("turnCount", turnCount);
            tttDifficulty = savedInstanceState.getInt(EXTRA_DIFFICULTY, tttDifficulty);
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
            hideOverlayImmediate();
        } else {
            button.setText("O");
            button.setTextColor(getResources().getColor(R.color.playerO));
            txtStatus.setText(R.string.turn_player_x);
            hideOverlayImmediate();
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
                    // Delay breve para que el overlay se vea
                    handler.postDelayed(this::cpuMakeMove, 300);
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

        // Asegurar que el overlay no quede visible al terminar
        hideOverlayImmediate();

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
                int puntosAward = (tttDifficulty == 0) ? 25 : (tttDifficulty == 1) ? 100 : 500;
                guardarPuntuacion("Tres en Raya - " + difficultyName(), puntosAward);
            }
        } else if (message != null && !message.equals(getString(R.string.game_draw))) {
            int puntosAward = (tttDifficulty == 0) ? 25 : (tttDifficulty == 1) ? 100 : 500;
            guardarPuntuacion("Tres en Raya - " + difficultyName(), puntosAward);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            int despuesTotal = antesTotal + puntos;
            ScoreMilestones.checkMilestones(this, username, antesTotal, despuesTotal);
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

        // Asegurar overlay oculto al reiniciar
        hideOverlayImmediate();

        // If PVC and CPU is X, CPU starts again
        if (gameMode.equals("PVC")) {
            if (!humanIsX) {
                cpuMakeMove();
            }
        }
    }

    private void hideOverlayImmediate() {
        if (hideOverlayRunnable != null) {
            handler.removeCallbacks(hideOverlayRunnable);
            hideOverlayRunnable = null;
        }
        if (overlayIcon != null) {
            overlayIcon.setVisibility(View.GONE);
            overlayIcon.setAlpha(1f);
            overlayIcon.setText("");
            overlayIcon.invalidate();
        }
    }

    private void cpuMakeMove() {
        // Movimiento según dificultad:
        // 0 - Fácil: aleatorio
        // 1 - Medio: intentar ganar; si no, bloquear; si no, aleatorio
        // 2 - Difícil: ganar > bloquear > centro > esquinas > lados

        List<Integer> emptyIndexes = new ArrayList<>();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getText().toString().equals("")) {
                emptyIndexes.add(i);
            }
        }
        if (emptyIndexes.isEmpty()) return;

        int choice = -1;

        if (tttDifficulty == 0) {
            // Fácil: aleatorio
            choice = emptyIndexes.get(random.nextInt(emptyIndexes.size()));
        } else if (tttDifficulty == 1) {
            // Medio: intentar ganar o bloquear
            choice = findWinningMoveFor(getCpuMark());
            if (choice == -1) choice = findWinningMoveFor(getHumanMark()); // bloquear
            if (choice == -1) choice = emptyIndexes.get(random.nextInt(emptyIndexes.size()));
        } else {
            // Difícil: ganar > bloquear > centro > esquinas > lados
            choice = findWinningMoveFor(getCpuMark());
            if (choice == -1) choice = findWinningMoveFor(getHumanMark());
            if (choice == -1 && buttons[4].getText().toString().equals("")) choice = 4; // centro
            if (choice == -1) {
                // esquinas
                int[] corners = {0,2,6,8};
                List<Integer> availableCorners = new ArrayList<>();
                for (int c : corners) if (buttons[c].getText().toString().equals("")) availableCorners.add(c);
                if (!availableCorners.isEmpty()) choice = availableCorners.get(random.nextInt(availableCorners.size()));
            }
            if (choice == -1) {
                // lados
                int[] sides = {1,3,5,7};
                List<Integer> availableSides = new ArrayList<>();
                for (int s : sides) if (buttons[s].getText().toString().equals("")) availableSides.add(s);
                if (!availableSides.isEmpty()) choice = availableSides.get(random.nextInt(availableSides.size()));
            }
            if (choice == -1) choice = emptyIndexes.get(random.nextInt(emptyIndexes.size()));
        }

        if (choice == -1) return;

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

    // Helpers para IA: obtener marcas
    private String getCpuMark() {
        if (gameMode.equals("PVC")) {
            // Si humano es X, CPU es O
            return humanIsX ? "O" : "X";
        }
        // En PVP no aplica, pero por seguridad devolvemos O
        return "O";
    }

    private String getHumanMark() {
        if (gameMode.equals("PVC")) {
            return humanIsX ? "X" : "O";
        }
        return "X";
    }

    // Busca movimiento que permita ganar para marca dada, o -1 si no existe
    private int findWinningMoveFor(String mark) {
        // simulamos colocar mark en cada vacío y comprobamos si produce victoria
        for (int i = 0; i < buttons.length; i++) {
            if (!buttons[i].getText().toString().equals("")) continue;
            buttons[i].setText(mark);
            boolean win = simulateCheckWin();
            buttons[i].setText("");
            if (win) return i;
        }
        return -1;
    }

    // Reusa lógica de checkForWin pero sobre el tablero actual (sin cambiar botones)
    private boolean simulateCheckWin() {
        String[][] field = new String[3][3];
        for (int i = 0; i < buttons.length; i++) field[i / 3][i % 3] = buttons[i].getText().toString();

        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1]) && field[i][0].equals(field[i][2]) && !field[i][0].equals("")) return true;
            if (field[0][i].equals(field[1][i]) && field[0][i].equals(field[2][i]) && !field[0][i].equals("")) return true;
        }
        if (field[0][0].equals(field[1][1]) && field[0][0].equals(field[2][2]) && !field[0][0].equals("")) return true;
        if (field[0][2].equals(field[1][1]) && field[0][2].equals(field[2][0]) && !field[0][2].equals("")) return true;
        return false;
    }

    private String difficultyName() {
        switch (tttDifficulty) {
            case 0: return "Fácil";
            case 1: return "Medio";
            default: return "Difícil";
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_GAME_MODE, gameMode);
        outState.putBoolean(EXTRA_HUMAN_IS_X, humanIsX);
        outState.putBoolean("playerXTurn", playerXTurn);
        outState.putInt("turnCount", turnCount);
        outState.putInt(EXTRA_DIFFICULTY, tttDifficulty);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Cancelar callbacks cuando la actividad se pausa para evitar overlays persistentes
        hideOverlayImmediate();
    }
}
