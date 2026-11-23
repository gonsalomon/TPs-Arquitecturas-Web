package org.example.fila_virtual.DTO.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngresarFilaRequestDTO {
    @NotNull(message = "El eventoId es obligatorio")
    private String eventoId;
}
