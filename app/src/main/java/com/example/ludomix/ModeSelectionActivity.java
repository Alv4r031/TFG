package com.example.ludomix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ModeSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        Button btnPvp = findViewById(R.id.button_pvp);
        Button btnPvc = findViewById(R.id.button_pvc);
        Button btnBack = findViewById(R.id.button_back_menu);

        btnPvp.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionActivity.this, TicTacToeActivity.class);
            intent.putExtra(TicTacToeActivity.EXTRA_GAME_MODE, "PVP");
            startActivity(intent);
        });

        btnPvc.setOnClickListener(v -> {
            boolean humanIsX = new Random().nextBoolean();
            Intent intent = new Intent(ModeSelectionActivity.this, TicTacToeActivity.class);
            intent.putExtra(TicTacToeActivity.EXTRA_GAME_MODE, "PVC");
            intent.putExtra(TicTacToeActivity.EXTRA_HUMAN_IS_X, humanIsX);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
