package org.example.entrada.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.entrada.utils.EstadoEntrada;

@AllArgsConstructor
@Getter
@Setter
public class ValidarEntradaResponse {
    private Long idEntrada;
    private String eventoId;
    private EstadoEntrada estado;
    private String mensaje;
}
