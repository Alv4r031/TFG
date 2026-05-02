package com.example.ludomix;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ludomix.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PUNTUACION = "puntuacion";

    public static final String TABLE_PUNTUACIONES = "puntuaciones";
    public static final String COLUMN_PUNTUACION_ID = "id";
    public static final String COLUMN_PUNTUACION_USERNAME = "username";
    public static final String COLUMN_PUNTUACION_PUNTOS = "puntos";
    public static final String COLUMN_PUNTUACION_JUEGO = "juego";
    public static final String COLUMN_PUNTUACION_FECHA = "fecha";

    private static final String CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_PUNTUACION + " INTEGER DEFAULT 0)";

    private static final String CREATE_TABLE_PUNTUACIONES =
            "CREATE TABLE " + TABLE_PUNTUACIONES + " (" +
                    COLUMN_PUNTUACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PUNTUACION_USERNAME + " TEXT NOT NULL, " +
                    COLUMN_PUNTUACION_PUNTOS + " INTEGER NOT NULL, " +
                    COLUMN_PUNTUACION_JUEGO + " TEXT NOT NULL, " +
                    COLUMN_PUNTUACION_FECHA + " LONG NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_PUNTUACION_USERNAME + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_USERNAME + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USUARIOS);
        db.execSQL(CREATE_TABLE_PUNTUACIONES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUNTUACIONES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }
}


