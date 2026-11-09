package org.example.reserva.dto.request;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SectorExternoDTO {
    private String nombre;
    private String tipo;
    private Integer filas;
    private Integer asientosPorFila;
    private Integer capacidad;
}
