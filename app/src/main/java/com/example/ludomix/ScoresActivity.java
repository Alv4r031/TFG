package com.example.ludomix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ScoresActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        Button btnBack = findViewById(R.id.btnScoresBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        TextView txtContent = findViewById(R.id.txtScoresContent);
        if (txtContent == null) return;

        // Leer estadísticas almacenadas localmente (demo)
        StringBuilder sb = new StringBuilder();
        sb.append("Piedra,Papel o Tijera:\n");
        sb.append("  Partidas: ").append(prefs.getInt("plays_rps", 0)).append("\n");
        sb.append("  Victorias: ").append(prefs.getInt("wins_rps", 0)).append("\n\n");

        sb.append("Tres en Raya:\n");
        sb.append("  Partidas: ").append(prefs.getInt("plays_ttt", 0)).append("\n");
        sb.append("  Victorias: ").append(prefs.getInt("wins_ttt", 0)).append("\n\n");

        sb.append("Adivina el Número:\n");
        sb.append("  Partidas: ").append(prefs.getInt("plays_guess", 0)).append("\n");
        sb.append("  Victorias: ").append(prefs.getInt("wins_guess", 0)).append("\n\n");

        sb.append("Juego de Memoria:\n");
        sb.append("  Partidas: ").append(prefs.getInt("plays_memory", 0)).append("\n");
        sb.append("  Victorias: ").append(prefs.getInt("wins_memory", 0)).append("\n");

        txtContent.setText(sb.toString());
    }
}
