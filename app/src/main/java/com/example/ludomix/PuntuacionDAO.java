package com.example.ludomix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PuntuacionDAO {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public PuntuacionDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Registra una nueva puntuación
     */
    public boolean registrarPuntuacion(Puntuacion puntuacion) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PUNTUACION_USERNAME, puntuacion.getUsername());
        values.put(DatabaseHelper.COLUMN_PUNTUACION_PUNTOS, puntuacion.getPuntos());
        values.put(DatabaseHelper.COLUMN_PUNTUACION_JUEGO, puntuacion.getJuego());
        values.put(DatabaseHelper.COLUMN_PUNTUACION_FECHA, puntuacion.getFecha());

        long resultado = db.insert(DatabaseHelper.TABLE_PUNTUACIONES, null, values);
        return resultado != -1;
    }

    /**
     * Obtiene todas las puntuaciones de un usuario ordenadas por fecha descendente
     */
    public Cursor obtenerPuntuacionesUsuario(String username) {
        return db.query(
                DatabaseHelper.TABLE_PUNTUACIONES,
                null,
                DatabaseHelper.COLUMN_PUNTUACION_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                DatabaseHelper.COLUMN_PUNTUACION_FECHA + " DESC"
        );
    }

    /**
     * Obtiene todas las puntuaciones de todos los usuarios
     */
    public Cursor obtenerTodasLasPuntuaciones() {
        return db.query(
                DatabaseHelper.TABLE_PUNTUACIONES,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_PUNTUACION_FECHA + " DESC"
        );
    }

    /**
     * Obtiene las mejores puntuaciones de un juego en particular
     */
    public Cursor obtenerMejoresPuntuacionesPorJuego(String juego) {
        return db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_PUNTUACIONES +
                " WHERE " + DatabaseHelper.COLUMN_PUNTUACION_JUEGO + " = ? " +
                "ORDER BY " + DatabaseHelper.COLUMN_PUNTUACION_PUNTOS + " DESC " +
                "LIMIT 10",
                new String[]{juego}
        );
    }

    /**
     * Obtiene el total de puntos de un usuario
     */
    public int obtenerPuntosTotal(String username) {
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COLUMN_PUNTUACION_PUNTOS + ") as total " +
                "FROM " + DatabaseHelper.TABLE_PUNTUACIONES +
                " WHERE " + DatabaseHelper.COLUMN_PUNTUACION_USERNAME + " = ?",
                new String[]{username}
        );

        if (!cursor.moveToFirst()) {
            cursor.close();
            return 0;
        }

        int total = cursor.isNull(cursor.getColumnIndexOrThrow("total"))
                ? 0
                : cursor.getInt(cursor.getColumnIndexOrThrow("total"));
        cursor.close();
        return total;
    }

    /**
     * Elimina todas las puntuaciones de un usuario
     */
    public boolean eliminarPuntuacionesUsuario(String username) {
        int rowsAffected = db.delete(
                DatabaseHelper.TABLE_PUNTUACIONES,
                DatabaseHelper.COLUMN_PUNTUACION_USERNAME + " = ?",
                new String[]{username}
        );

        return rowsAffected > 0;
    }
}

