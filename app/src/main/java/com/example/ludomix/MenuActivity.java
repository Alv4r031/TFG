package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Asignar listeners a los botones del menú
        Button btnGuessNumber = findViewById(R.id.btnGuessNumber);
        Button btnRps = findViewById(R.id.btnRps);
        Button btnTicTacToe = findViewById(R.id.btnTicTacToe);
        Button btnMemoryGame = findViewById(R.id.btnMemoryGame);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnScores = findViewById(R.id.btnScores);

        btnGuessNumber.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GuessNumberDifficultyActivity.class);
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
            Intent intent = new Intent(MenuActivity.this, ModeSelectionActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Actividad no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnMemoryGame.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MemoryDifficultyActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Actividad no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón Login / Logout
        btnLogin.setOnClickListener(v -> {
            String logged = prefs.getString(KEY_LOGGED_IN, null);
            if (logged == null) {
                // No hay sesión -> abrir pantalla de login/registro
                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                // Hay sesión -> cerrar sesión
                prefs.edit().remove(KEY_LOGGED_IN).apply();
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                // Actualizar UI
                updateScoresButtonVisibility(btnScores);
                updateLoginButtonText(btnLogin);
            }
        });

        btnScores.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ScoresActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Actividad no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // Ajustar visibilidad del botón puntuaciones y texto del login según sesión
        updateScoresButtonVisibility(btnScores);
        updateLoginButtonText(btnLogin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button btnScores = findViewById(R.id.btnScores);
        Button btnLogin = findViewById(R.id.btnLogin);
        updateScoresButtonVisibility(btnScores);
        updateLoginButtonText(btnLogin);
    }

    private void updateScoresButtonVisibility(Button btnScores) {
        if (btnScores == null) return;
        String logged = prefs.getString(KEY_LOGGED_IN, null);
        if (logged != null) {
            btnScores.setVisibility(Button.VISIBLE);
        } else {
            btnScores.setVisibility(Button.GONE);
        }
    }

    private void updateLoginButtonText(Button btnLogin) {
        if (btnLogin == null) return;
        String logged = prefs.getString(KEY_LOGGED_IN, null);
        if (logged != null) {
            btnLogin.setText("Cerrar sesión");
        } else {
            btnLogin.setText(R.string.login_signin);
        }
    }
}
