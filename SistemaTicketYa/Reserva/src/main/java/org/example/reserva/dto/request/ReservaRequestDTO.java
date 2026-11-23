package org.example.reserva.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaRequestDTO {



    @NotNull(message = "Se necesita un cliente valido")
    private Long idCliente;

    @NotNull(message = "Se necesita un evento valido")
    private String idEvento;

    @NotNull(message = "Se necesita un sector valido")
    private String sector;

    private List<Long> asientos;

    private Integer cantidad;

    private Long idOrden;
}
