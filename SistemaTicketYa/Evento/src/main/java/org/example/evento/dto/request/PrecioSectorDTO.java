package org.example.evento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrecioSectorDTO {
    @NotBlank(message = "El sector es obligatorio")
    private String sector;
    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero
    private Double precio;
}
