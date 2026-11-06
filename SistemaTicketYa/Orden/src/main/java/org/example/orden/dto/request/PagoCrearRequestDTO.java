package org.example.orden.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoCrearRequestDTO {
    private Long idReserva;
    private Long idCliente;
    private String metodoPago;
}
