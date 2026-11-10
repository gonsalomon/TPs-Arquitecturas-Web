package org.example.entrada.client.pago.response.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.entrada.client.utils.Estado;
import org.example.entrada.client.utils.MetodoPago;


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
