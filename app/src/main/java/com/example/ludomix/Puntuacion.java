package com.example.ludomix;

public class Puntuacion {
    private int id;
    private String username;
    private int puntos;
    private String juego;
    private long fecha;

    public Puntuacion() {
    }

    public Puntuacion(String username, int puntos, String juego) {
        this.username = username;
        this.puntos = puntos;
        this.juego = juego;
        this.fecha = System.currentTimeMillis();
    }

    public Puntuacion(int id, String username, int puntos, String juego, long fecha) {
        this.id = id;
        this.username = username;
        this.puntos = puntos;
        this.juego = juego;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public String getJuego() {
        return juego;
    }

    public void setJuego(String juego) {
        this.juego = juego;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }
}
