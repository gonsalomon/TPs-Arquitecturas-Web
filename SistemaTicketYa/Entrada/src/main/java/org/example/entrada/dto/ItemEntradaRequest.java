package org.example.entrada.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemEntradaRequest(
        @NotNull Long sectorId,
        Integer fila,      // null if the sector is not numbered
        Integer asiento,   // null if the sector is not numbered
        @NotNull @Positive BigDecimal precio
) {}