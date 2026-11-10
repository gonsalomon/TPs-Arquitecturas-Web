package org.example.orden.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.orden.utils.MedioPago;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoCrearRequestDTO {
    private Long idReserva;
    private Long idCliente;
    private MedioPago metodoPago;
    private BigDecimal monto;
}
