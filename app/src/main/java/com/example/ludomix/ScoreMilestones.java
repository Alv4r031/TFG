package com.example.ludomix;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class ScoreMilestones {

    // Añadido 3000 y 3500 al conjunto de hitos
    private static final int[] MILESTONES = {3000, 3500, 5000, 10000, 50000, 100000};

    public static void checkMilestones(Context context, String username, int beforeTotal, int afterTotal) {
        if (username == null || context == null) return;

        // Buscar thresholds cruzados
        StringBuilder reached = new StringBuilder();
        for (int m : MILESTONES) {
            if (beforeTotal < m && afterTotal >= m) {
                if (reached.length() > 0) reached.append("\n");
                reached.append("Has alcanzado ").append(m).append(" puntos!");
            }
        }

        if (reached.length() == 0) return; // no hay hitos alcanzados

        // Mostrar diálogo en UI thread
        if (context instanceof Activity) {
            Activity act = (Activity) context;
            Handler h = new Handler(Looper.getMainLooper());
            h.post(() -> {
                new AlertDialog.Builder(act)
                        .setTitle("¡Enhorabuena!")
                        .setMessage(reached.toString())
                        .setPositiveButton("OK", null)
                        .show();
            });
        }
    }
}
