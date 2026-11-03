package org.example.entrada.utils;

public enum EstadoEntrada {
    VALIDA(true),
    USADA(true),
    EN_DEVOLUCION(true),   // client requested refund, admin hasn't decided
    REINTEGRO(false);      // seat/spot released, money to be refunded

    private final boolean ocupaLugar;
    EstadoEntrada(boolean ocupaLugar) { this.ocupaLugar = ocupaLugar; }
    public boolean ocupaLugar() { return ocupaLugar; }
}