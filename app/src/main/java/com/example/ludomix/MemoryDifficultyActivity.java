package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ludomix.R;

public class MemoryDifficultyActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY";
    public static final int DIFF_EASY = 0;
    public static final int DIFF_MEDIUM = 1;
    public static final int DIFF_HARD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_difficulty);

        Button btnEasy = (Button) findViewById(R.id.btnEasy);
        Button btnMedium = (Button) findViewById(R.id.btnMedium);
        Button btnHard = (Button) findViewById(R.id.btnHard);

        TextView txtEasyStatus = findViewById(R.id.txtEasyStatus);
        TextView txtMediumStatus = findViewById(R.id.txtMediumStatus);
        TextView txtHardStatus = findViewById(R.id.txtHardStatus);

        // Obtener puntuaciones actuales del usuario y decidir desbloqueos
        SharedPreferences prefs = getSharedPreferences("ludomix_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("logged_in_user", null);
        int easyScore = 0;
        int mediumScore = 0;
        if (username != null) {
            PuntuacionDAO puntuacionDAO = new PuntuacionDAO(this);
            puntuacionDAO.open();
            android.database.Cursor cursor = puntuacionDAO.obtenerPuntuacionesUsuario(username);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String juego = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_JUEGO));
                    int puntos = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS));
                    if ("Memoria - Fácil".equals(juego)) {
                        easyScore += puntos;
                    } else if ("Memoria - Medio".equals(juego)) {
                        mediumScore += puntos;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            puntuacionDAO.close();
        }

        // Configurar estado de botones
        btnEasy.setEnabled(true);
        txtEasyStatus.setText("Puntos: " + easyScore + " / 500");

        boolean mediumUnlocked = easyScore >= 500;
        btnMedium.setEnabled(mediumUnlocked);
        if (mediumUnlocked) {
            txtMediumStatus.setText("Puntos: " + mediumScore + " / 1000");
        } else {
            txtMediumStatus.setText("Bloqueado - Necesitas 500 puntos en Fácil");
            btnMedium.setAlpha(0.5f);
        }

        boolean hardUnlocked = mediumScore >= 1000;
        btnHard.setEnabled(hardUnlocked);
        if (hardUnlocked) {
            txtHardStatus.setText("Desbloqueado");
        } else {
            txtHardStatus.setText("Bloqueado - Necesitas 1000 puntos en Medio");
            btnHard.setAlpha(0.5f);
        }

        btnEasy.setOnClickListener(v -> startMemoryWith(DIFF_EASY));
        btnMedium.setOnClickListener(v -> startMemoryWith(DIFF_MEDIUM));
        btnHard.setOnClickListener(v -> startMemoryWith(DIFF_HARD));

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void startMemoryWith(int difficulty) {
        Intent intent = new Intent(this, MemoryGameActivity.class);
        intent.putExtra(EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
        finish();
    }
}
