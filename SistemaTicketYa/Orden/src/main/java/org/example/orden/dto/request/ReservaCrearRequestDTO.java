package org.example.orden.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaCrearRequestDTO {
    private Long idCliente;
    private String idEvento;
    private String sector;
    private List<Long> asientos;
    private Integer cantidad;
}
