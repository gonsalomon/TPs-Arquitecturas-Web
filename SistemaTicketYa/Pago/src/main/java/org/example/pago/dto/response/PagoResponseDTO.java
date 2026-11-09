package org.example.pago.dto.response;

import lombok.*;
import org.example.pago.utils.Estado;
import org.example.pago.utils.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
public class PagoResponseDTO {
    private Long idPago;
    private Long idCliente;
    private Long idReserva;
    private Estado estado;
    private MetodoPago metodoPago;
    private BigDecimal monto;
    private LocalDateTime fecha_emision;
}
