package org.example.evento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.evento.utils.TipoSector;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SectorRequestDTO {
    @NotBlank(message = "El nombre del sector es obligatorio")
    private String nombre;
    @NotNull(message = "El tipo de sector es obligatorio")
    private TipoSector tipo;
    @Positive private Integer filas;
    @Positive private Integer asientosPorFila;
    @Positive private Integer capacidad;
}
