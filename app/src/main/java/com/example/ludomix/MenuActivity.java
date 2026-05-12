package com.example.ludomix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";
    private final Handler handler = new Handler();

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
        Button btnScores = findViewById(R.id.btnScores);
        TextView txtUsername = findViewById(R.id.txtUsername);
        ImageView imgLogoLogout = findViewById(R.id.imgLogoLogout);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnGuessNumber.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GuessNumberDifficultyActivity.class);
            playButtonAndLaunch(v, intent);
        });

        btnRps.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, RpsActivity.class);
            playButtonAndLaunch(v, intent);
        });

        btnTicTacToe.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ModeSelectionActivity.class);
            playButtonAndLaunch(v, intent);
        });

        btnMemoryGame.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MemoryDifficultyActivity.class);
            playButtonAndLaunch(v, intent);
        });

        btnScores.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ScoresActivity.class);
            playButtonAndLaunch(v, intent);
        });

        // Click largo en logo para confirmar logout
        imgLogoLogout.setOnClickListener(v -> {
            // Si no hay sesión, ir a Login
            String logged = prefs.getString(KEY_LOGGED_IN, null);
            if (logged == null) {
                startActivity(new Intent(MenuActivity.this, LoginActivity.class));
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Deseas cerrar sesión?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        prefs.edit().remove(KEY_LOGGED_IN).apply();
                        Toast.makeText(MenuActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Click en botón textual para cerrar sesión (misma lógica que en la imagen)
        btnLogout.setOnClickListener(v -> {
            String logged = prefs.getString(KEY_LOGGED_IN, null);
            if (logged == null) {
                startActivity(new Intent(MenuActivity.this, LoginActivity.class));
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Deseas cerrar sesión?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        prefs.edit().remove(KEY_LOGGED_IN).apply();
                        Toast.makeText(MenuActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Ajustar visibilidad del botón puntuaciones y mostrar nombre de usuario si existe
        updateScoresButtonVisibility(btnScores);
        updateHeaderUser(txtUsername, imgLogoLogout);
    }

    private void playButtonAndLaunch(View v, Intent intent) {
        // Animación simple al presionar
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_press_anim);
        v.startAnimation(anim);

        handler.postDelayed(() -> {
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Actividad no disponible", Toast.LENGTH_SHORT).show();
            }
        }, 90); // pequeño delay para dejar ver la animación
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button btnScores = findViewById(R.id.btnScores);
        TextView txtUsername = findViewById(R.id.txtUsername);
        ImageView imgLogoLogout = findViewById(R.id.imgLogoLogout);
        Button btnLogout = findViewById(R.id.btnLogout);
        updateScoresButtonVisibility(btnScores);
        updateHeaderUser(txtUsername, imgLogoLogout);
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

    private void updateHeaderUser(TextView txtUsername, ImageView imgLogoLogout) {
        String logged = prefs.getString(KEY_LOGGED_IN, null);
        if (logged != null) {
            txtUsername.setText(logged);
            if (!logged.isEmpty()) {
                imgLogoLogout.setContentDescription(String.valueOf(logged.charAt(0)).toUpperCase());
            }
        } else {
            txtUsername.setText(R.string.guest);
            imgLogoLogout.setContentDescription(getString(R.string.avatar_letter));
        }
    }
}
