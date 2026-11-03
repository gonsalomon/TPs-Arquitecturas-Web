package org.example.entrada.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record EmitirEntradasRequest(
        @NotNull Long ordenId,
        @NotNull Long eventoId,
        @NotNull Long clienteId,
        @NotNull LocalDateTime fechaEvento,
        @NotEmpty @Valid List<ItemEntradaRequest> items
) {}