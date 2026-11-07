package org.example.evento.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.evento.entity.PrecioSector;
import org.example.evento.utils.EstadoEvento;
import org.example.evento.utils.TipoEvento;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class EventoResponseDTO {
    private String idEvento;
    private String nombre;
    private String descripcion;
    private TipoEvento tipo;
    private Long idOrganizador;
    private List<String> participantes;
    private LocalDateTime fechaHora;
    private String idLugar;
    private String nombreLugar;
    private List<PrecioSector> precios;
    private EstadoEvento estado;
}
