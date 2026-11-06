package org.example.orden.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoExternoDTO {
    private String idEvento;
    private String estado;
    private LocalDateTime fechaHora;
    private String idLugar;
    private List<PrecioSectorExternoDTO> precios;
}
