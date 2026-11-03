package org.example.entrada.dto;

import org.example.entrada.utils.EstadoEntrada;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EntradaDTO(
        Long id,
        Long eventoId,
        Long sectorId,
        Long clienteId,
        Long ordenId,
        Integer fila,
        Integer asiento,
        BigDecimal precio,
        LocalDateTime fechaEvento,
        EstadoEntrada estado,
        String codigoQr
) {}