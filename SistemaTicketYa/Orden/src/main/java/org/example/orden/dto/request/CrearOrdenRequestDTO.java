package org.example.orden.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearOrdenRequestDTO {

    @NotBlank(message = "El eventoId es obligatorio")
    private String eventoId;

    @NotBlank(message = "El sector es obligatorio")
    private String sector;

    /** Para sectores numerados: fila y asiento de cada lugar pedido. */
    private List<AsientoRequestDTO> asientos;

    /** Para sectores no numerados: cantidad de lugares pedidos. */
    private Integer cantidad;
}
