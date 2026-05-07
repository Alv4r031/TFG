package com.example.ludomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity {

    // Número de cartas ahora configurable según dificultad
    private int numCards = 16;
    private int difficulty = MemoryDifficultyActivity.DIFF_MEDIUM;
    private String difficultyName = "Medio";
    private final List<String> cardValues = new ArrayList<>();
    private final List<Button> cards = new ArrayList<>();

    private Button firstCard = null;
    private Button secondCard = null;
    private int pairsFound = 0;
    private int attempts = 0; // turns (cada vez que se prueban dos cartas)
    private boolean isBusy = false; // evita que el usuario pulse cartas mientras se procesan

    private TextView statusTextView;
    private GridLayout memoryGrid;
    private TextView txtMemoryProgress;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";
    private static final String KEY_LOGGED_IN = "logged_in_user";

    private UsuarioDAO usuarioDAO;
    private PuntuacionDAO puntuacionDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        puntuacionDAO = new PuntuacionDAO(this);
        puntuacionDAO.open();

        statusTextView = findViewById(R.id.txtMemoryStatus);
        memoryGrid = findViewById(R.id.memoryGrid);
        int resIdProgress = getResources().getIdentifier("txtMemoryProgress", "id", getPackageName());
        if (resIdProgress != 0) {
            txtMemoryProgress = findViewById(resIdProgress);
        } else {
            txtMemoryProgress = null; // layout variante no contiene el id
        }

        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        Button btnBackToDifficulty = findViewById(R.id.btnBackToDifficulty);
        if (btnBackToDifficulty != null) {
            btnBackToDifficulty.setOnClickListener(v -> {
                Intent intent = new Intent(this, MemoryDifficultyActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (memoryGrid == null) {
            Toast.makeText(this, "GridLayout no encontrado en el layout", Toast.LENGTH_LONG).show();
            return;
        }

        // Leer dificultad enviada por MemoryDifficultyActivity
        try {
            difficulty = getIntent().getIntExtra(MemoryDifficultyActivity.EXTRA_DIFFICULTY, MemoryDifficultyActivity.DIFF_MEDIUM);
            switch (difficulty) {
                case MemoryDifficultyActivity.DIFF_EASY:
                    // Fácil = 2x2 (4 cartas)
                    numCards = 4; // 2x2
                    memoryGrid.setColumnCount(2);
                    difficultyName = "Fácil";
                    break;
                case MemoryDifficultyActivity.DIFF_HARD:
                    // Cambiado a 5x5 para mejor visualización
                    numCards = 25; // 5x5
                    memoryGrid.setColumnCount(5);
                    difficultyName = "Difícil";
                    break;
                case MemoryDifficultyActivity.DIFF_MEDIUM:
                default:
                    numCards = 16; // 4x4
                    memoryGrid.setColumnCount(4);
                    difficultyName = "Medio";
                    break;
            }
        } catch (Exception e) {
            // Fallback por si algo falla
            numCards = 16;
            memoryGrid.setColumnCount(4);
            difficultyName = "Medio";
        }

        // Asegurar que haya columnas (fallback si el atributo no se leyó)
        if (memoryGrid.getColumnCount() <= 0) {
            memoryGrid.setColumnCount(4);
        }

        startNewGame();
        updateMemoryProgress();
    }

    private void startNewGame() {
        // Limpiar estado anterior
        pairsFound = 0;
        attempts = 0;
        firstCard = null;
        secondCard = null;
        cards.clear();
        memoryGrid.removeAllViews();
        statusTextView.setText(R.string.memory_start_prompt);
        updateMemoryProgress();

        // Símbolos disponibles (suficientes hasta 36 cartas -> 18 pares)
        String[] symbols = {"🐶", "🐱", "🐵", "🦁", "🐯", "🦊", "🐨", "🐼", "🐸", "🐔", "🦄", "🐷", "🐮", "🐗", "🐝", "🦋", "🐞", "🐌", "🐙", "🐬"};
        cardValues.clear();

        int pairsNeeded = numCards / 2; // number of full pairs
        // Tomar los primeros pairsNeeded símbolos y duplicarlos
        for (int i = 0; i < pairsNeeded; i++) {
            String symbol = symbols[i % symbols.length];
            cardValues.add(symbol);
            cardValues.add(symbol);
        }

        // Si numCards es impar, añadimos una carta comodín (sin pareja)
        if (numCards % 2 == 1) {
            cardValues.add("★"); // carta comodín — nunca empareja
        }

        Collections.shuffle(cardValues);

        // Determinar tamaño en dp según columnas para que el tablero quepa mejor en pantalla
        int cols = memoryGrid.getColumnCount();
        int sizeDp;
        if (cols >= 6) {
            sizeDp = 48; // muchas cartas -> cartas más pequeñas
        } else if (cols == 5) {
            sizeDp = 60; // 5 columnas -> tamaño intermedio
        } else if (cols == 4) {
            sizeDp = 72;
        } else if (cols == 3) {
            sizeDp = 88;
        } else {
            sizeDp = 80;
        }

        final float scale = getResources().getDisplayMetrics().density;
        final int sizePx = (int) (sizeDp * scale + 0.5f);

        // Calcular filas necesarias y asignarlas para evitar excepciones
        int rowsNeeded = (numCards + cols - 1) / cols;
        memoryGrid.setRowCount(rowsNeeded);

        // Crear y añadir los botones (cartas) al GridLayout
        for (int i = 0; i < numCards; i++) {
            Button card = new Button(this);
            card.setTag(cardValues.get(i)); // Guardamos el valor en el tag
            card.setOnClickListener(this::revealCard);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            // Establecer tamaño fijo en px para que se vean correctamente
            params.width = sizePx;
            params.height = sizePx;
            // Especificar fila y columna explícitamente
            int col = i % cols;
            int row = i / cols;
            params.columnSpec = GridLayout.spec(col, 1);
            params.rowSpec = GridLayout.spec(row, 1);
            params.setMargins(6, 6, 6, 6);
            card.setLayoutParams(params);

            // Ajustes de texto y apariencia
            card.setTextSize(TypedValue.COMPLEX_UNIT_SP, Math.max(12, sizeDp / 4));
            card.setTextColor(getResources().getColor(android.R.color.black));
            card.setBackgroundResource(R.drawable.card_background);
            card.setText(""); // Ocultar el contenido hasta que se revele
            card.setMinWidth(sizePx);
            card.setMinHeight(sizePx);

            cards.add(card);
            memoryGrid.addView(card);
        }

        // Forzar layout/update
        memoryGrid.requestLayout();
        memoryGrid.invalidate();

        // Debug: actualizar status con número de cartas añadidas
        statusTextView.setText(getString(R.string.memory_start_prompt) + " (" + memoryGrid.getChildCount() + " cartas)");
    }

    public void revealCard(View view) {
        if (isBusy) return; // Ignorar clicks mientras procesamos
        Button selectedCard = (Button) view;

        // No hacer nada si se pulsa una carta ya revelada o si ya hay 2 cartas levantadas
        if (selectedCard.getText().length() > 0 || secondCard != null) {
            return;
        }

        selectedCard.setText((String) selectedCard.getTag());

        if (firstCard == null) {
            firstCard = selectedCard;
        } else {
            secondCard = selectedCard;
            // Contabilizar intento (se han levantado dos cartas)
            attempts++;
            // Actualizar estado en pantalla
            statusTextView.setText(getString(R.string.memory_pair_attempts, attempts));
            // Bloquear nuevas interacciones hasta resolver el par
            isBusy = true;
            checkForMatch();
        }
    }

    private void checkForMatch() {
        // Si alguna carta es el comodín (★) no puede emparejar
        Object tag1 = firstCard.getTag();
        Object tag2 = secondCard.getTag();

        // Si ambas cartas tienen tag y son iguales y no son comodín => es un par
        if (tag1 != null && tag2 != null && tag1.equals(tag2) && !"★".equals(tag1)) {
            // ¡Es un par! (sin mostrar texto en pantalla)
            firstCard.setEnabled(false); // Deshabilita para que no se pueda volver a pulsar
            secondCard.setEnabled(false);

            pairsFound++;

            // Desbloquear inmediatamente para seguir jugando
            isBusy = false;

            if (pairsFound == numCards / 2) {
                // Se han encontrado todos los pares: victoria
                statusTextView.setText(R.string.memory_win);

                // Juego completado: actualizar estadísticas
                try {
                    int plays = prefs.getInt("plays_memory", 0);
                    int wins = prefs.getInt("wins_memory", 0);
                    prefs.edit().putInt("plays_memory", plays + 1).putInt("wins_memory", wins + 1).apply();
                    Toast.makeText(this, "Estadísticas actualizadas", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Guardar puntuación según dificultad
                int puntosAGuardar;
                if (difficulty == MemoryDifficultyActivity.DIFF_EASY) {
                    // Fácil: recompensa fija
                    puntosAGuardar = 100;
                } else {
                    // MEDIO y DIFÍCIL: aplicar nueva tabla basada en attempts
                    if (attempts < 5) {
                        puntosAGuardar = 200;
                    } else if (attempts < 10) {
                        puntosAGuardar = 100;
                    } else if (attempts < 15) {
                        puntosAGuardar = 75;
                    } else {
                        puntosAGuardar = 50;
                    }
                }

                // Comprobar desbloqueos: obtener total antes
                int antesTotal = 0;
                String username = prefs.getString(KEY_LOGGED_IN, null);
                if (username != null) {
                    PuntuacionDAO dao = new PuntuacionDAO(this);
                    dao.open();
                    android.database.Cursor cursor = dao.obtenerPuntuacionesUsuario(username);
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            String juego = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_JUEGO));
                            int pts = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS));
                            if (("Memoria - Fácil".equals(juego) && difficulty == MemoryDifficultyActivity.DIFF_EASY) || ("Memoria - Medio".equals(juego) && difficulty == MemoryDifficultyActivity.DIFF_MEDIUM)) {
                                antesTotal += pts;
                            }
                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                    dao.close();
                }

                guardarPuntuacion("Memoria - " + difficultyName, puntosAGuardar);

                int despuesTotal = antesTotal + puntosAGuardar;
                if (difficulty == MemoryDifficultyActivity.DIFF_EASY && antesTotal < 500 && despuesTotal >= 500) {
                    Toast.makeText(this, "¡Has desbloqueado el nivel Medio!", Toast.LENGTH_LONG).show();
                } else if (difficulty == MemoryDifficultyActivity.DIFF_MEDIUM && antesTotal < 1000 && despuesTotal >= 1000) {
                    Toast.makeText(this, "¡Has desbloqueado el nivel Difícil!", Toast.LENGTH_LONG).show();
                }

                // Actualizar indicador de progreso tras guardar puntos
                updateMemoryProgress();
                resetTurn();
            } else {
                // Par encontrado pero todavía quedan más pares
                resetTurn();
            }
        } else {
            // No es un par: ocultar las dos cartas después de un breve retraso
            statusTextView.setText(R.string.memory_no_match);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (firstCard != null) firstCard.setText("");
                if (secondCard != null) secondCard.setText("");
                resetTurn();
                // Desbloquear interacción tras ocultar
                isBusy = false;
            }, 1000); // 1 segundo de retraso
        }
    }

    private void resetTurn() {
        firstCard = null;
        secondCard = null;
    }

    public void resetMemoryGame(View view) {
        startNewGame();
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

    private void guardarPuntuacion(String juego, int puntos) {
        String username = prefs.getString(KEY_LOGGED_IN, null);
        if (username == null) return;

        Puntuacion puntuacion = new Puntuacion(username, puntos, juego);
        if (puntuacionDAO.registrarPuntuacion(puntuacion)) {
            Usuario usuario = usuarioDAO.obtenerUsuario(username);
            if (usuario != null) {
                usuarioDAO.actualizarPuntuacion(username, usuario.getPuntuacion() + puntos);
            }
            updateMemoryProgress();
        }
    }

    private void updateMemoryProgress() {
        if (txtMemoryProgress == null) return;
        String username = prefs.getString(KEY_LOGGED_IN, null);
        if (username == null) {
            txtMemoryProgress.setText("Inicia sesión para ver progreso");
            return;
        }

        // Calcular puntos acumulados para la familia 'Memoria'
        PuntuacionDAO dao = new PuntuacionDAO(this);
        dao.open();
        int totalMemoria = 0;
        android.database.Cursor cursor = dao.obtenerPuntuacionesUsuario(username);
        String expectedJuego = "Memoria - " + difficultyName; // sumar solo para la dificultad actual
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String juego = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_JUEGO));
                if (juego != null && juego.trim().equals(expectedJuego)) {
                    totalMemoria += cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        dao.close();

        // Determinar umbral siguiente según dificultad actual
        int siguienteUmbral;
        if (difficulty == MemoryDifficultyActivity.DIFF_EASY) {
            siguienteUmbral = 500;
        } else if (difficulty == MemoryDifficultyActivity.DIFF_MEDIUM) {
            siguienteUmbral = 1000;
        } else {
            // Si estamos en Difícil no hay siguiente umbral: mostramos el total como objetivo (X / X)
            siguienteUmbral = totalMemoria;
        }

        // Mostrar en formato sencillo solicitado
        txtMemoryProgress.setText("Puntos para siguiente nivel: " + totalMemoria + " / " + siguienteUmbral);
    }
}
