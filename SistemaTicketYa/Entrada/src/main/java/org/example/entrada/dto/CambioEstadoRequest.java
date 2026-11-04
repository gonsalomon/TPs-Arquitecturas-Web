package org.example.entrada.dto;

import jakarta.validation.constraints.NotNull;
import org.example.entrada.utils.EstadoEntrada;
import org.example.entrada.utils.MotivoCambio;

// motivo is mandatory when estado = REINTEGRO
public record CambioEstadoRequest(
        @NotNull EstadoEntrada estado,
        MotivoCambio motivo
) {}