package org.example.evento.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.evento.utils.EstadoEvento;
import org.example.evento.utils.TipoEvento;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "eventos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Evento {
    @Id
    private String idEvento;

    private String nombre;
    private String descripcion;
    private TipoEvento tipo;
    private Long idOrganizador;              // id del usuario (ms Usuario)
    private List<String> participantes = new ArrayList<>();
    private LocalDateTime fechaHora;
    private String idLugar;                  // referencia al Lugar
    private List<PrecioSector> precios = new ArrayList<>();
    private EstadoEvento estado = EstadoEvento.BORRADOR;
}
