package org.example.orden.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EntradaDTO(
        Long id,
        String eventoId,
        String sectorId,
        Long clienteId,
        Long ordenId,
        Integer fila,
        Integer asiento,
        BigDecimal precio,
        LocalDateTime fechaEvento,
        String estado,
        String codigoQr
) {}