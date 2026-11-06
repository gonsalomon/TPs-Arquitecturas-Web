package org.example.orden.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoOrdenTest {

    @Test
    void soloPendientePagoAprobadoYConfirmadaSonActivas() {
        assertTrue(EstadoOrden.PENDIENTE_PAGO.esActiva());
        assertTrue(EstadoOrden.PAGO_APROBADO.esActiva());
        assertTrue(EstadoOrden.CONFIRMADA.esActiva());
        assertFalse(EstadoOrden.RECHAZADA.esActiva());
        assertFalse(EstadoOrden.VENCIDA.esActiva());
    }
}
