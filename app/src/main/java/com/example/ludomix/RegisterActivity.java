package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private UsuarioDAO usuarioDAO;
    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        EditText etNewUser = findViewById(R.id.etNewUser);
        EditText etNewEmail = findViewById(R.id.etNewEmail);
        EditText etNewPassword = findViewById(R.id.etNewPassword);
        Button btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {
            String username = etNewUser.getText().toString().trim();
            String email = etNewEmail.getText().toString().trim();
            String password = etNewPassword.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Usuario usuario = new Usuario(username, email, password);
            boolean ok = usuarioDAO.registrarUsuario(usuario);
            if (ok) {
                // Guardar sesión automáticamente
                prefs.edit().putString(KEY_LOGGED_IN, username).apply();

                Toast.makeText(this, "Cuenta creada e iniciada. Bienvenido", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) usuarioDAO.close();
    }
}
