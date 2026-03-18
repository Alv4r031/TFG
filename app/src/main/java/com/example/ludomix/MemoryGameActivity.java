package com.example.ludomix;

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

    private final int NUM_CARDS = 16;
    private final List<String> cardValues = new ArrayList<>();
    private final List<Button> cards = new ArrayList<>();

    private Button firstCard = null;
    private Button secondCard = null;
    private int pairsFound = 0;

    private TextView statusTextView;
    private GridLayout memoryGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        statusTextView = findViewById(R.id.txtMemoryStatus);
        memoryGrid = findViewById(R.id.memoryGrid);

        Button btnBack = findViewById(R.id.btnBackToMenu);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (memoryGrid == null) {
            Toast.makeText(this, "GridLayout no encontrado en el layout", Toast.LENGTH_LONG).show();
            return;
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

        // Preparar los valores de las cartas (8 pares)
        String[] symbols = {"🐶", "🐱", "🐵", "🦁", "🐯", "🦊", "🐨", "🐼"};
        cardValues.clear();
        for (String symbol : symbols) {
            cardValues.add(symbol);
            cardValues.add(symbol);
        }
        Collections.shuffle(cardValues);

        // Tamaño en px para cada carta (80dp)
        final int sizeDp = 80;
        final float scale = getResources().getDisplayMetrics().density;
        final int sizePx = (int) (sizeDp * scale + 0.5f);

        // Crear y añadir los botones (cartas) al GridLayout
        int cols = memoryGrid.getColumnCount();
        for (int i = 0; i < NUM_CARDS; i++) {
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
            params.columnSpec = GridLayout.spec(col);
            params.rowSpec = GridLayout.spec(row);
            params.setMargins(8, 8, 8, 8);
            card.setLayoutParams(params);

            // Ajustes de texto y apariencia
            card.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
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
        if (firstCard.getTag().equals(secondCard.getTag())) {
            // ¡Es un par!
            statusTextView.setText(R.string.memory_pair_found);
            firstCard.setEnabled(false); // Deshabilita para que no se pueda volver a pulsar
            secondCard.setEnabled(false);

            pairsFound++;

            if (pairsFound == NUM_CARDS / 2) {
                statusTextView.setText(R.string.memory_win);
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
