package org.example.entrada.utils;

// Why a ticket goes to REINTEGRO. Only DEVOLUCION is subject to the 72h rule.
public enum MotivoCambio {
    DEVOLUCION,         // client refund approved by an administrator
    EVENTO_CANCELADO,   // the event was cancelled
    ANULACION_ORDEN     // compensation: Orden undoes an issuance because a later step failed
}