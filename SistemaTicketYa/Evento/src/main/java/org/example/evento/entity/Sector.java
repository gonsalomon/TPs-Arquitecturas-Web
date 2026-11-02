package org.example.evento.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.evento.utils.TipoSector;

/* Va embebido dentro del Lugar (no es una coleccion aparte).
 * NUMERADO: usa filas y asientosPorFila. NO_NUMERADO: usa capacidad (ej. "Campo"). */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sector {
    private String nombre;
    private TipoSector tipo;
    private Integer filas;
    private Integer asientosPorFila;
    private Integer capacidad;

}
