package org.example.entrada.dto;

import lombok.*;
import org.example.entrada.utils.EstadoEvento;

@Getter
@AllArgsConstructor
public class RevisionEntradaRequestDTO {
    private EstadoEvento estado;
    private Long idReserva;
}
