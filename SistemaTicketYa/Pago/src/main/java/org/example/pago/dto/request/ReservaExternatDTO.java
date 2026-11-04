package org.example.pago.dto.request;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReservaExternatDTO {
    private Long idReserva;
    private Long idCliente;
    private String idEvento;
    private List<Long> asientos;
    private String estado; // O el Enum si lo compartes
    private LocalDateTime fechaVencimiento;
    private Float montoTotal;
}
