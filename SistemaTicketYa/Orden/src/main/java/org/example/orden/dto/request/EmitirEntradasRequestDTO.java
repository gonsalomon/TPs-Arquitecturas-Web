package org.example.orden.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmitirEntradasRequestDTO {
    private Long ordenId;
    private String eventoId;
    private Long clienteId;
    private LocalDateTime fechaEvento;
    private List<ItemEmitirDTO> items;
}
