package org.example.orden.utils;

public enum EstadoOrden {
    PENDIENTE_PAGO,
    PAGO_APROBADO,
    CONFIRMADA,
    RECHAZADA,
    VENCIDA;

    public boolean esActiva() {
        return this == PENDIENTE_PAGO || this == PAGO_APROBADO || this == CONFIRMADA;
    }
}
