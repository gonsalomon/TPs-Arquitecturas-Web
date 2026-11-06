package org.example.orden.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoExternoDTO {
    private Long idPago;
    private Long idCliente;
    private Long idReserva;
    private String estado;
    private String metodoPago;
    private LocalDateTime fechaEmision;
}
