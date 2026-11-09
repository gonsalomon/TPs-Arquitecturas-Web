package org.example.reserva.dto.request;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventoExternoDTO {
    private String id;
    private String idLugar;
    private String estadoEvento;
    private float precio;
}
