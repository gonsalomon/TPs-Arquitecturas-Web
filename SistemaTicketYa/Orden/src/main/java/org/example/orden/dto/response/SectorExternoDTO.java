package org.example.orden.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectorExternoDTO {
    private String nombre;
    private String tipo;
    private Integer filas;
    private Integer asientosPorFila;
    private Integer capacidad;
}
