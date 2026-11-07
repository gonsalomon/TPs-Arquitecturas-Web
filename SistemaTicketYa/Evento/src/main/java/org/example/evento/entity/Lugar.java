package org.example.evento.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "lugares")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lugar {
    @Id
    private String idLugar;

    @Indexed(unique = true)          // el nombre es unico: se usa para buscar
    private String nombre;
    private String direccion;
    private String ciudad;
    private Double latitud;
    private Double longitud;
    private List<Sector> sectores = new ArrayList<>();

    public Lugar(String nombre, String direccion, String ciudad, Double latitud, Double longitud, List<Sector> sectores) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.sectores = sectores;
    }
}
