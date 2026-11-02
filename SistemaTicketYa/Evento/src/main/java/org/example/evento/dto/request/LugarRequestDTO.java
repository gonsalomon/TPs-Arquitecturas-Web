package org.example.evento.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LugarRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "La direccion es obligatoria")
    private String direccion;
    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;
    private Double latitud;
    private Double longitud;
    @NotEmpty(message = "El lugar debe tener al menos un sector")
    private List<@Valid SectorRequestDTO> sectores;
}
