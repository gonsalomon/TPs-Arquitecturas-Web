package org.example.evento.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoRecomendadoDTO {
    private String eventoId;
    private String nombre;
    private String tipo;
    private String ciudad;
    private LocalDateTime fechaHora;
    private List<String> participantes;
    private int puntaje;
    private List<String> motivos;
}