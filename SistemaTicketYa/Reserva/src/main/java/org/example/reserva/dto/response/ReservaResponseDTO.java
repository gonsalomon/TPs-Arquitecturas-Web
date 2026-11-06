package org.example.reserva.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.reserva.utils.EstadoReserva;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReservaResponseDTO {
    private Long idReserva;
    private Long idCliente;
    private String idEvento;
    private String sector;
    private List<Long> asientos;
    private EstadoReserva estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaVencimiento;
}
