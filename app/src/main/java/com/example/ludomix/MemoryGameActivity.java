package com.example.ludomix;

import android.content.Context;
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
    private final List<String> cardValues = new ArrayList<>();
    private final List<Button> cards = new ArrayList<>();

    private Button firstCard = null;
    private Button secondCard = null;
    private int pairsFound = 0;

    private TextView statusTextView;
    private GridLayout memoryGrid;

    private SharedPreferences prefs;
    private static final String PREFS = "ludomix_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        statusTextView = findViewById(R.id.txtMemoryStatus);
        memoryGrid = findViewById(R.id.memoryGrid);

        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (memoryGrid == null) {
            Toast.makeText(this, "GridLayout no encontrado en el layout", Toast.LENGTH_LONG).show();
            return;
        }

        // Leer dificultad enviada por MemoryDifficultyActivity
        try {
            int difficulty = getIntent().getIntExtra(MemoryDifficultyActivity.EXTRA_DIFFICULTY, MemoryDifficultyActivity.DIFF_MEDIUM);
            switch (difficulty) {
                case MemoryDifficultyActivity.DIFF_EASY:
                    // Fácil = 2x2 (4 cartas)
                    numCards = 4; // 2x2
                    memoryGrid.setColumnCount(2);
                    break;
                case MemoryDifficultyActivity.DIFF_HARD:
                    // Cambiado a 5x5 para mejor visualización
                    numCards = 25; // 5x5
                    memoryGrid.setColumnCount(5);
                    break;
                case MemoryDifficultyActivity.DIFF_MEDIUM:
                default:
                    numCards = 16; // 4x4
                    memoryGrid.setColumnCount(4);
                    break;
            }
        } catch (Exception e) {
            // Fallback por si algo falla
            numCards = 16;
            memoryGrid.setColumnCount(4);
        }

        // Asegurar que haya columnas (fallback si el atributo no se leyó)
        if (memoryGrid.getColumnCount() <= 0) {
            memoryGrid.setColumnCount(4);
        }

        startNewGame();
    }

    private void startNewGame() {
        // Limpiar estado anterior
        pairsFound = 0;
        firstCard = null;
        secondCard = null;
        cards.clear();
        memoryGrid.removeAllViews();
        statusTextView.setText(R.string.memory_start_prompt);

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
            checkForMatch();
        }
    }

    private void checkForMatch() {
        // Si alguna carta es el comodín (★) no puede emparejar
        Object tag1 = firstCard.getTag();
        Object tag2 = secondCard.getTag();
        if (tag1 != null && tag2 != null && tag1.equals(tag2) && !"★".equals(tag1)) {
            // ¡Es un par!
            statusTextView.setText(R.string.memory_pair_found);
            firstCard.setEnabled(false); // Deshabilita para que no se pueda volver a pulsar
            secondCard.setEnabled(false);

            pairsFound++;

            if (pairsFound == numCards / 2) {
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
            }

            resetTurn();
        } else {
            // No es un par
            statusTextView.setText(R.string.memory_no_match);

            // Usamos un Handler para ocultar las cartas después de un breve retraso
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                firstCard.setText("");
                secondCard.setText("");
                resetTurn();
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
}
