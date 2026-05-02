package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtUsername, edtPassword;
    private boolean isLoginMode = true;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";
    
    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        
        // Inicializar DAO
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();

        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnAction = findViewById(R.id.btnAction);
        Button btnShowLogin = findViewById(R.id.btnShowLogin);
        Button btnShowRegister = findViewById(R.id.btnShowRegister);
        Button btnBack = findViewById(R.id.btnBack);

        btnShowLogin.setOnClickListener(v -> switchToLogin());
        btnShowRegister.setOnClickListener(v -> switchToRegister());

        btnAction.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String user = edtUsername.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            
            if (isLoginMode) {
                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Verificar si el usuario existe en la BD
                if (!usuarioDAO.usuarioExiste(user)) {
                    Toast.makeText(this, "Usuario no registrado. Por favor regístrate primero", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Validar credenciales
                if (usuarioDAO.validarLogin(user, pass)) {
                    // Login correcto
                    prefs.edit().putString(KEY_LOGGED_IN, user).apply();
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                    // Abrir MenuActivity
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Registro: validar todos los campos
                if (email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Validar formato de email simple
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Crear nuevo usuario
                Usuario nuevoUsuario = new Usuario(user, email, pass);
                
                if (usuarioDAO.registrarUsuario(nuevoUsuario)) {
                    Toast.makeText(this, "¡Registro completado!", Toast.LENGTH_SHORT).show();
                    // Realizar login automático
                    prefs.edit().putString(KEY_LOGGED_IN, user).apply();
                    // Abrir MenuActivity
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Usuario ya existe", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
    }

    private void switchToLogin() {
        isLoginMode = true;
        Button btnAction = findViewById(R.id.btnAction);
        btnAction.setText(R.string.login_signin);
        edtEmail.setVisibility(View.GONE);
        edtEmail.setText("");
        edtUsername.setText("");
        edtPassword.setText("");
    }

    private void switchToRegister() {
        isLoginMode = false;
        Button btnAction = findViewById(R.id.btnAction);
        btnAction.setText(R.string.login_register);
        edtEmail.setVisibility(View.VISIBLE);
        edtEmail.setText("");
        edtUsername.setText("");
        edtPassword.setText("");
    }
}
