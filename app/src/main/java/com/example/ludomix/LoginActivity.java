package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private boolean isLoginMode = true;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnAction = findViewById(R.id.btnAction);
        Button btnShowLogin = findViewById(R.id.btnShowLogin);
        Button btnShowRegister = findViewById(R.id.btnShowRegister);
        Button btnBack = findViewById(R.id.btnBack);

        btnShowLogin.setOnClickListener(v -> switchToLogin());
        btnShowRegister.setOnClickListener(v -> switchToRegister());

        btnAction.setOnClickListener(v -> {
            String user = edtUsername.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_your_guess), Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                // Comprueba si existe usuario guardado (demo en SharedPreferences)
                String stored = prefs.getString("user_" + user, null);
                if (stored != null && stored.equals(pass)) {
                    // Login correcto
                    prefs.edit().putString(KEY_LOGGED_IN, user).apply();
                    Toast.makeText(this, R.string.login_signin, Toast.LENGTH_SHORT).show();
                    // Abrir MenuActivity
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Registro: guardar credenciales en SharedPreferences (demo)
                String already = prefs.getString("user_" + user, null);
                if (already != null) {
                    Toast.makeText(this, "Usuario ya existe", Toast.LENGTH_SHORT).show();
                } else {
                    prefs.edit().putString("user_" + user, pass).putString(KEY_LOGGED_IN, user).apply();
                    Toast.makeText(this, "Registro completado y sesión iniciada", Toast.LENGTH_SHORT).show();
                    // Abrir MenuActivity
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            // Si hay sesión activa, este botón cerrará sesión; si no, simplemente cierra la activity
            if (prefs.getString(KEY_LOGGED_IN, null) != null) {
                prefs.edit().remove(KEY_LOGGED_IN).apply();
                Toast.makeText(this, R.string.login_logout, Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        // Si ya hay sesión, abrir directamente el menú
        String logged = prefs.getString(KEY_LOGGED_IN, null);
        if (logged != null) {
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void switchToLogin() {
        isLoginMode = true;
        Button btnAction = findViewById(R.id.btnAction);
        btnAction.setText(R.string.login_signin);
    }

    private void switchToRegister() {
        isLoginMode = false;
        Button btnAction = findViewById(R.id.btnAction);
        btnAction.setText(R.string.login_register);
    }
}
