package org.example.evento.mapper;

import org.example.evento.dto.request.EventoRequestDTO;
import org.example.evento.dto.request.PrecioSectorDTO;
import org.example.evento.dto.response.EventoResponseDTO;
import org.example.evento.entity.Evento;
import org.example.evento.entity.PrecioSector;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventoMapper {

    public EventoResponseDTO convertToDTO(Evento evento, String nombreLugar) {
        return new EventoResponseDTO(
                evento.getIdEvento(),
                evento.getNombre(),
                evento.getDescripcion(),
                evento.getTipo(),
                evento.getIdOrganizador(),
                evento.getParticipantes(),
                evento.getFechaHora(),
                evento.getIdLugar(),
                nombreLugar,
                evento.getPrecios(),
                evento.getEstado()
        );
    }

    /* Copia los datos del request al evento (sirve para crear y para editar). */
    public void copiarDatos(EventoRequestDTO dto, Evento evento) {
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setTipo(dto.getTipo());
//        evento.setIdOrganizador(dto.getIdOrganizador());
        evento.setParticipantes(dto.getParticipantes() == null ? new ArrayList<>() : dto.getParticipantes());
        evento.setFechaHora(dto.getFechaHora());
        evento.setIdLugar(dto.getIdLugar());
        evento.setPrecios(convertPrecios(dto.getPrecios()));
    }

    private List<PrecioSector> convertPrecios(List<PrecioSectorDTO> precios) {
        if (precios == null) return new ArrayList<>();
        return precios.stream().map(p -> new PrecioSector(p.getSector(), p.getPrecio())).toList();
    }
}
