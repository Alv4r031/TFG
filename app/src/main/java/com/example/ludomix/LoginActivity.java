package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnAction, btnShowLogin, btnShowRegister, btnBack;
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
        btnAction = findViewById(R.id.btnAction);
        btnShowLogin = findViewById(R.id.btnShowLogin);
        btnShowRegister = findViewById(R.id.btnShowRegister);
        btnBack = findViewById(R.id.btnBack);

        btnShowLogin.setOnClickListener(v -> switchToLogin());
        btnShowRegister.setOnClickListener(v -> switchToRegister());

        btnAction.setOnClickListener(v -> {
            String user = edtUsername.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Introduce usuario y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                // Comprueba si existe usuario guardado (demo en SharedPreferences)
                String stored = prefs.getString("user_" + user, null);
                if (stored != null && stored.equals(pass)) {
                    // Login correcto
                    prefs.edit().putString(KEY_LOGGED_IN, user).apply();
                    Toast.makeText(this, "Sesión iniciada", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
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
                    prefs.edit().putString("user_" + user, pass).apply();
                    prefs.edit().putString(KEY_LOGGED_IN, user).apply();
                    Toast.makeText(this, "Registro completado y sesión iniciada", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        btnBack.setOnClickListener(v -> finish());

        // Si ya hay sesión, cerrar inmediatamente con OK
        if (prefs.getString(KEY_LOGGED_IN, null) != null) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void switchToLogin() {
        isLoginMode = true;
        btnAction.setText("Iniciar sesión");
    }

    private void switchToRegister() {
        isLoginMode = false;
        btnAction.setText("Registrar");
    }
}
