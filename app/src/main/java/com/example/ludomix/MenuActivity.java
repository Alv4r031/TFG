package com.example.ludomix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Asignar listeners a los botones del menú
        Button btnGuessNumber = findViewById(R.id.btnGuessNumber);
        Button btnRps = findViewById(R.id.btnRps);
        Button btnTicTacToe = findViewById(R.id.btnTicTacToe);
        Button btnMemoryGame = findViewById(R.id.btnMemoryGame);

        btnGuessNumber.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GuessNumberActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Actividad no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnRps.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, RpsActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Actividad no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnTicTacToe.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, TicTacToeActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Actividad no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnMemoryGame.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MemoryGameActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Actividad no disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
