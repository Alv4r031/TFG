package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GuessNumberDifficultyActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY";
    public static final int DIFF_EASY = 0;
    public static final int DIFF_MEDIUM = 1;
    public static final int DIFF_HARD = 2;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_EASY_SCORE = "guess_easy_score";
    private static final String KEY_MEDIUM_SCORE = "guess_medium_score";

    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_difficulty);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();

        Button btnEasy = findViewById(R.id.btnEasy);
        Button btnMedium = findViewById(R.id.btnMedium);
        Button btnHard = findViewById(R.id.btnHard);
        Button btnBack = findViewById(R.id.btnBack);

        TextView txtEasyStatus = findViewById(R.id.txtEasyStatus);
        TextView txtMediumStatus = findViewById(R.id.txtMediumStatus);
        TextView txtHardStatus = findViewById(R.id.txtHardStatus);

        // Obtener puntuaciones actuales del usuario
        String username = prefs.getString("logged_in_user", null);
        int easyScore = 0;
        int mediumScore = 0;

        if (username != null) {
            usuarioDAO.open();
            PuntuacionDAO puntuacionDAO = new PuntuacionDAO(this);
            puntuacionDAO.open();

            // Calcular puntos en nivel Fácil
            android.database.Cursor cursorEasy = puntuacionDAO.obtenerPuntuacionesUsuario(username);
            if (cursorEasy != null && cursorEasy.moveToFirst()) {
                do {
                    String juego = cursorEasy.getString(
                            cursorEasy.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_JUEGO)
                    );
                    if ("Adivina Número - Fácil".equals(juego)) {
                        int puntos = cursorEasy.getInt(
                                cursorEasy.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS)
                        );
                        easyScore += puntos;
                    }
                } while (cursorEasy.moveToNext());
                cursorEasy.close();
            }

            // Calcular puntos en nivel Medio
            android.database.Cursor cursorMedium = puntuacionDAO.obtenerPuntuacionesUsuario(username);
            if (cursorMedium != null && cursorMedium.moveToFirst()) {
                do {
                    String juego = cursorMedium.getString(
                            cursorMedium.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_JUEGO)
                    );
                    if ("Adivina Número - Medio".equals(juego)) {
                        int puntos = cursorMedium.getInt(
                                cursorMedium.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS)
                        );
                        mediumScore += puntos;
                    }
                } while (cursorMedium.moveToNext());
                cursorMedium.close();
            }
            puntuacionDAO.close();
        }

        // Configurar botones y estados
        // Fácil siempre disponible
        btnEasy.setEnabled(true);
        txtEasyStatus.setText("Puntos: " + easyScore + " / 500");

        // Medio desbloqueado si Fácil >= 500
        boolean mediumUnlocked = easyScore >= 500;
        btnMedium.setEnabled(mediumUnlocked);
        if (mediumUnlocked) {
            txtMediumStatus.setText("Puntos: " + mediumScore + " / 1000");
        } else {
            txtMediumStatus.setText("Bloqueado - Necesitas 500 puntos en Fácil");
            btnMedium.setAlpha(0.5f);
        }

        // Difícil desbloqueado si Medio >= 1000
        boolean hardUnlocked = mediumScore >= 1000;
        btnHard.setEnabled(hardUnlocked);
        if (hardUnlocked) {
            txtHardStatus.setText("Desbloqueado");
        } else {
            txtHardStatus.setText("Bloqueado - Necesitas 1000 puntos en Medio");
            btnHard.setAlpha(0.5f);
        }

        btnEasy.setOnClickListener(v -> startGuessNumberWith(DIFF_EASY));
        btnMedium.setOnClickListener(v -> {
            if (mediumUnlocked) {
                startGuessNumberWith(DIFF_MEDIUM);
            } else {
                Toast.makeText(this, "Debes alcanzar 500 puntos en Fácil", Toast.LENGTH_SHORT).show();
            }
        });
        btnHard.setOnClickListener(v -> {
            if (hardUnlocked) {
                startGuessNumberWith(DIFF_HARD);
            } else {
                Toast.makeText(this, "Debes alcanzar 1000 puntos en Medio", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void startGuessNumberWith(int difficulty) {
        Intent intent = new Intent(this, GuessNumberActivity.class);
        intent.putExtra(EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
    }
}

