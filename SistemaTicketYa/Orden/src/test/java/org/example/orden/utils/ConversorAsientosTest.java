package org.example.orden.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConversorAsientosTest {

    @Test
    void convierteFilaYAsientoAlNumeroDeReserva() {
        assertEquals(1, ConversorAsientos.numeroDeAsiento(1, 1, 20));
        assertEquals(45, ConversorAsientos.numeroDeAsiento(3, 5, 20));
        assertEquals(40, ConversorAsientos.numeroDeAsiento(2, 20, 20));
    }
}
