package org.example.orden.utils;

public final class ConversorAsientos {

    private ConversorAsientos() {
    }

    public static int numeroDeAsiento(int fila, int asiento, int asientosPorFila) {
        return (fila - 1) * asientosPorFila + asiento;
    }
}
