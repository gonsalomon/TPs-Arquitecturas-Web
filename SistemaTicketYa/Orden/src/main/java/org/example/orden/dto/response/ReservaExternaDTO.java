package org.example.orden.dto.response;

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
public class ReservaExternaDTO {
    private Long idReserva;
    private Long idCliente;
    private String idEvento;
    private String sector;
    private List<Long> asientos;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaVencimiento;
}
