package org.example.fila_virtual.DTO.Request;

import lombok.Data;

@Data
public class EventoExternoDTO {
    private String id;
    private String nombre;
    private String estado;
    private Boolean altaDemanda;
}
