package org.example.entrada.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

// Sent by Orden once the payment is approved. Idempotent by ordenId.
public record EmitirEntradasRequest(
        @NotNull Long ordenId,
        @NotBlank String eventoId,
        @NotNull Long clienteId,
        @NotNull LocalDateTime fechaEvento,
        @NotEmpty @Valid List<ItemEntradaRequest> items
) {}