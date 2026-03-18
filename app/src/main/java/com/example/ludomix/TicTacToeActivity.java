package com.example.ludomix;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TicTacToeActivity extends AppCompatActivity {

    private Button[] buttons = new Button[9];
    private boolean playerXTurn = true; // True = X, False = O
    private int turnCount = 0;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe);

        txtStatus = findViewById(R.id.txtStatus);

        for (int i = 0; i < buttons.length; i++) {
            String buttonID = "btn" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
        }

        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    public void onCellClick(View v) {
        Button button = (Button) v;

        // Si la celda ya está ocupada o el juego terminó, no hacer nada
        if (!button.getText().toString().equals("")) {
            return;
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
    }

    public void resetGame(View view) {
        playerXTurn = true;
        turnCount = 0;
        txtStatus.setText(R.string.turn_player_x);
        for (Button button : buttons) {
            button.setText("");
            button.setEnabled(true);
        }
    }
}
