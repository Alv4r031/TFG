package com.example.ludomix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UsuarioDAO {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public UsuarioDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Registra un nuevo usuario en la base de datos
     * @return true si se registró correctamente, false si el usuario ya existe
     */
    public boolean registrarUsuario(Usuario usuario) {
        // Verificar si el usuario ya existe
        if (usuarioExiste(usuario.getUsername())) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, usuario.getUsername());
        values.put(DatabaseHelper.COLUMN_EMAIL, usuario.getEmail());
        values.put(DatabaseHelper.COLUMN_PASSWORD, usuario.getPassword());
        values.put(DatabaseHelper.COLUMN_PUNTUACION, usuario.getPuntuacion());

        long resultado = db.insert(DatabaseHelper.TABLE_USUARIOS, null, values);
        return resultado != -1;
    }

    /**
     * Verifica si un usuario existe en la base de datos
     */
    public boolean usuarioExiste(String username) {
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USUARIOS,
                null,
                DatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );

        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    /**
     * Valida el login del usuario
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public boolean validarLogin(String username, String password) {
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USUARIOS,
                null,
                DatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );

        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        }

        cursor.moveToFirst();
        String passwordGuardada = cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)
        );
        cursor.close();

        return passwordGuardada.equals(password);
    }

    /**
     * Obtiene un usuario por su nombre de usuario
     */
    public Usuario obtenerUsuario(String username) {
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USUARIOS,
                null,
                DatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );

        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String user = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
        int puntuacion = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUNTUACION));

        cursor.close();
        return new Usuario(id, user, email, password, puntuacion);
    }

    /**
     * Actualiza la puntuación de un usuario
     */
    public boolean actualizarPuntuacion(String username, int nuevaPuntuacion) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PUNTUACION, nuevaPuntuacion);

        int rowsAffected = db.update(
                DatabaseHelper.TABLE_USUARIOS,
                values,
                DatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[]{username}
        );

        return rowsAffected > 0;
    }

    /**
     * Obtiene todos los usuarios
     */
    public Cursor obtenerTodosLosUsuarios() {
        return db.query(
                DatabaseHelper.TABLE_USUARIOS,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_PUNTUACION + " DESC"
        );
    }
}

