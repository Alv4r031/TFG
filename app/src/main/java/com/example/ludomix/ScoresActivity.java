package com.example.ludomix;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ScoresActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";
    
    private UsuarioDAO usuarioDAO;
    private PuntuacionDAO puntuacionDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        
        // Inicializar DAOs
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        
        puntuacionDAO = new PuntuacionDAO(this);
        puntuacionDAO.open();

        Button btnBack = findViewById(R.id.btnScoresBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
        if (puntuacionDAO != null) {
            puntuacionDAO.close();
        }
    }

    private void loadStats() {
        TextView txtContent = findViewById(R.id.txtScoresContent);
        if (txtContent == null) return;

        String username = prefs.getString(KEY_LOGGED_IN, null);
        if (username == null) {
            txtContent.setText(R.string.no_active_user);
            return;
        }

        StringBuilder sb = new StringBuilder();
        
        // Obtener información del usuario
        Usuario usuario = usuarioDAO.obtenerUsuario(username);
        if (usuario != null) {
            sb.append("Usuario: ").append(usuario.getUsername()).append("\n");
            sb.append("Email: ").append(usuario.getEmail()).append("\n");
            sb.append("Puntuación Total: ").append(puntuacionDAO.obtenerPuntosTotal(username)).append("\n\n");
        }

        // Obtener puntuaciones por juego
        sb.append("=== PUNTUACIONES POR JUEGO ===\n\n");
        
        String[] juegos = {"Piedra Papel Tijera", "Tres en Raya", "Adivina Número", "Memoria"};
        
        for (String juego : juegos) {
            Cursor cursor = puntuacionDAO.obtenerPuntuacionesUsuario(username);
            int contador = 0;
            int puntosTotales = 0;
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String juegoDb = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_JUEGO)
                    );
                    if (juegoDb.equals(juego)) {
                        contador++;
                        int puntos = cursor.getInt(
                                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS)
                        );
                        puntosTotales += puntos;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            
            sb.append(juego).append(":\n");
            sb.append("  Partidas: ").append(contador).append("\n");
            sb.append("  Puntos Totales: ").append(puntosTotales).append("\n\n");
        }

        txtContent.setText(sb.toString());
    }
}
