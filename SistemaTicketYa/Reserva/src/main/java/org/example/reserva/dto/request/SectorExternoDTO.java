package org.example.reserva.dto.request;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SectorExternoDTO {
    private String nombre;
    private float precio;
    private List<Long> asientosPermitidos;
}
