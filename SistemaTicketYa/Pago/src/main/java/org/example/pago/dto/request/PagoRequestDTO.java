package org.example.pago.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import org.example.pago.utils.MetodoPago;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
public class PagoRequestDTO {

    @NotNull(message = "Se necesita una reserva valida")
    private Long idReserva;

    @NotNull(message = "Se necesita un cliente valido")
    private Long idCliente;

    @NotNull(message = "Se necesita un metodo de pago")
    private MetodoPago metodoPago;

    @NotNull(message = "Se necesita un monto valido")
    private BigDecimal monto;
}
