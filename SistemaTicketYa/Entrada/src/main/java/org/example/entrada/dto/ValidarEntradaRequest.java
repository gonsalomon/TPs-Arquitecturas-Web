package org.example.entrada.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ValidarEntradaRequest {
    @NotBlank
    private String codigoQr;
    @NotBlank
    private String eventoId;
}
