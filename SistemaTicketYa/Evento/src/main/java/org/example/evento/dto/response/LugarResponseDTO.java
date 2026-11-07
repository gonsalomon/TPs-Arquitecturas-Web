package org.example.evento.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.evento.entity.Sector;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LugarResponseDTO {
    private String idLugar;
    private String nombre;
    private String direccion;
    private String ciudad;
    private Double latitud;
    private Double longitud;
    private List<Sector> sectores;
}
