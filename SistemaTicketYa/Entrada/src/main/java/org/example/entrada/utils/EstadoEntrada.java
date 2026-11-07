package org.example.entrada.utils;

public enum EstadoEntrada {
    VALIDA(true),
    USADA(true),
    REINTEGRO(false);   // ticket void, seat/spot free again, money to be returned (handled by Orden/Pago)

    private final boolean ocupaLugar;

    EstadoEntrada(boolean ocupaLugar) {
        this.ocupaLugar = ocupaLugar;
    }

    public boolean ocupaLugar() {
        return ocupaLugar;
    }

    // USADA and REINTEGRO are terminal
    public boolean puedePasarA(EstadoEntrada nuevo) {
        return this == VALIDA && (nuevo == USADA || nuevo == REINTEGRO);
    }
}