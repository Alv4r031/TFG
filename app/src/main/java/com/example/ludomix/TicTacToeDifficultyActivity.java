package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TicTacToeDifficultyActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY";
    public static final int DIFF_EASY = 0;
    public static final int DIFF_MEDIUM = 1;
    public static final int DIFF_HARD = 2;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";

    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe_difficulty);

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

        // Mostrar cuántos puntos otorga cada victoria; no hay desbloqueos por puntos
        if (txtEasyStatus != null) txtEasyStatus.setText(getString(R.string.ttt_award_points, 25));
        if (txtMediumStatus != null) txtMediumStatus.setText(getString(R.string.ttt_award_points, 100));
        if (txtHardStatus != null) txtHardStatus.setText(getString(R.string.ttt_award_points, 500));

        // Mantener los 3 botones siempre habilitados
        btnEasy.setEnabled(true);
        btnMedium.setEnabled(true);
        btnHard.setEnabled(true);

        btnEasy.setOnClickListener(v -> startModeSelectionWith(DIFF_EASY));
        btnMedium.setOnClickListener(v -> startModeSelectionWith(DIFF_MEDIUM));
        btnHard.setOnClickListener(v -> startModeSelectionWith(DIFF_HARD));

        btnBack.setOnClickListener(v -> finish());
    }

    private void startModeSelectionWith(int difficulty) {
        Intent intent = new Intent(this, ModeSelectionActivity.class);
        intent.putExtra(EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) usuarioDAO.close();
    }
}
