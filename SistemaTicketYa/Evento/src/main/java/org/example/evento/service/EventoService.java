package org.example.evento.service;

import lombok.AllArgsConstructor;
import org.example.evento.dto.request.EventoRequestDTO;
import org.example.evento.dto.request.PrecioSectorDTO;
import org.example.evento.dto.response.EventoResponseDTO;
import org.example.evento.entity.Evento;
import org.example.evento.entity.Lugar;
import org.example.evento.mapper.EventoMapper;
import org.example.evento.repository.EventoRepository;
import org.example.evento.utils.EstadoEvento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventoService {
    private final EventoRepository repository;
    private final LugarService lugarService;
    private final EventoMapper mapper;

    /*-------------------------- MÉTODOS PARA EL CRUD --------------------------*/
    /* Todo evento se crea en BORRADOR. El lugar tiene que existir antes. */
    public EventoResponseDTO save(EventoRequestDTO request) {
        Lugar lugar = lugarService.buscar(request.getIdLugar());
        validarPrecios(request.getPrecios(), lugar);
        Evento evento = new Evento();
        mapper.copiarDatos(request, evento);
        evento.setEstado(EstadoEvento.BORRADOR);
        return mapper.convertToDTO(repository.save(evento), lugar.getNombre());
    }

    public List<EventoResponseDTO> findAll(EstadoEvento estado) {
        List<Evento> eventos = estado == null ? repository.findAll() : repository.findByEstado(estado);
        return eventos.stream().map(this::toDTO).toList();
    }

    public EventoResponseDTO findById(String idEvento) {
        return toDTO(buscar(idEvento));
    }

    /* Solo se edita mientras esta en BORRADOR (despues de publicado ya se venden entradas). */
    public EventoResponseDTO update(String idEvento, EventoRequestDTO request) {
        Evento evento = buscar(idEvento);
        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new IllegalStateException("Solo se puede editar un evento en estado BORRADOR");
        }
        Lugar lugar = lugarService.buscar(request.getIdLugar());
        validarPrecios(request.getPrecios(), lugar);
        mapper.copiarDatos(request, evento);
        return mapper.convertToDTO(repository.save(evento), lugar.getNombre());
    }

    public void delete(String idEvento) {
        Evento evento = buscar(idEvento);
        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new IllegalStateException("Solo se puede borrar un evento en BORRADOR; si ya se publico, hay que cancelarlo");
        }
        repository.delete(evento);
    }

    /*-------------------------- CAMBIO DE ESTADO --------------------------*/
    public EventoResponseDTO cambiarEstado(String idEvento, EstadoEvento nuevo) {
        Evento evento = buscar(idEvento);
        EstadoEvento actual = evento.getEstado();
        if (!transicionValida(actual, nuevo)) {
            throw new IllegalStateException("No se puede pasar de " + actual + " a " + nuevo);
        }
        if (nuevo == EstadoEvento.PUBLICADO) {
            Lugar lugar = lugarService.buscar(evento.getIdLugar());
            if (evento.getPrecios().size() != lugar.getSectores().size()) {
                throw new IllegalStateException("Para publicar, todos los sectores del lugar deben tener precio");
            }
        }
        evento.setEstado(nuevo);
        return toDTO(repository.save(evento));
    }

    /* BORRADOR -> PUBLICADO -> AGOTADO / FINALIZADO. Se puede CANCELAR mientras no haya finalizado.
     * AGOTADO puede volver a PUBLICADO si se liberan lugares (devoluciones). */
    private boolean transicionValida(EstadoEvento actual, EstadoEvento nuevo) {
        return switch (actual) {
            case BORRADOR -> nuevo == EstadoEvento.PUBLICADO || nuevo == EstadoEvento.CANCELADO;
            case PUBLICADO -> nuevo == EstadoEvento.AGOTADO || nuevo == EstadoEvento.FINALIZADO || nuevo == EstadoEvento.CANCELADO;
            case AGOTADO -> nuevo == EstadoEvento.PUBLICADO || nuevo == EstadoEvento.FINALIZADO || nuevo == EstadoEvento.CANCELADO;
            case CANCELADO, FINALIZADO -> false;
        };
    }

    /*-------------------------- AUXILIARES --------------------------*/
    private Evento buscar(String idEvento) {
        return repository.findById(idEvento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el evento con el id: " + idEvento));
    }

    private EventoResponseDTO toDTO(Evento evento) {
        String nombreLugar = lugarService.buscar(evento.getIdLugar()).getNombre();
        return mapper.convertToDTO(evento, nombreLugar);
    }

    /* Cada precio debe corresponder a un sector que exista en el lugar, sin repetir. */
    private void validarPrecios(List<PrecioSectorDTO> precios, Lugar lugar) {
        if (precios == null) return;
        Set<String> sectoresLugar = lugar.getSectores().stream()
                .map(s -> s.getNombre().toLowerCase())
                .collect(Collectors.toSet());
        Set<String> vistos = new java.util.HashSet<>();
        for (PrecioSectorDTO p : precios) {
            String sector = p.getSector().toLowerCase();
            if (!sectoresLugar.contains(sector)) {
                throw new IllegalArgumentException("El sector " + p.getSector() + " no existe en " + lugar.getNombre());
            }
            if (!vistos.add(sector)) {
                throw new IllegalArgumentException("Precio repetido para el sector " + p.getSector());
            }
        }
    }
}
