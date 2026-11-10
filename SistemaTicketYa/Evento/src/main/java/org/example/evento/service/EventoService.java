package org.example.evento.service;

import lombok.AllArgsConstructor;
import org.example.evento.client.UsuarioFeingClient;
import org.example.evento.client.usuario.dto.response.UsuarioResponseDTO;
import org.example.evento.client.utils.Rol;
import org.example.evento.config.CacheConfig;
import org.example.evento.dto.request.BusquedaEventoDTO;
import org.example.evento.dto.request.EventoRequestDTO;
import org.example.evento.dto.request.PrecioSectorDTO;
import org.example.evento.dto.response.EventoResponseDTO;
import org.example.evento.entity.Evento;
import org.example.evento.entity.Lugar;
import org.example.evento.mapper.EventoMapper;
import org.example.evento.repository.EventoRepository;
import org.example.evento.entity.PrecioSector;
import org.example.evento.utils.EstadoEvento;
import org.example.evento.utils.OrdenBusqueda;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventoService {
    private final EventoRepository repository;
    private final LugarService lugarService;
    private final EventoMapper mapper;
    private final MongoTemplate mongoTemplate;
    private final UsuarioFeingClient  usuarioFeingClient;

    /*-------------------------- MÉTODOS PARA EL CRUD --------------------------*/
    /* Todo evento se crea en BORRADOR. El lugar tiene que existir antes. */
    public EventoResponseDTO save(EventoRequestDTO request, Long  usuarioId) {
        ResponseEntity<UsuarioResponseDTO> response = usuarioFeingClient.getById(usuarioId);
        UsuarioResponseDTO usuario = response.getBody();
        if(usuario == null){
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        if(usuario.getRol() != Rol.ORGANIZADOR){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no es un organizador");
        }
        Lugar lugar = lugarService.buscar(request.getIdLugar());
        validarPrecios(request.getPrecios(), lugar);
        Evento evento = new Evento();
        mapper.copiarDatos(request, evento);
        evento.setIdOrganizador(usuarioId);
        evento.setEstado(EstadoEvento.BORRADOR);
        return mapper.convertToDTO(repository.save(evento), lugar.getNombre());
    }

    public List<EventoResponseDTO> findAll(EstadoEvento estado) {
        List<Evento> eventos = estado == null ? repository.findAll() : repository.findByEstado(estado);
        return eventos.stream().map(this::toDTO).toList();
    }

    @Cacheable(value = CacheConfig.EVENTOS, key = "#idEvento")
    public EventoResponseDTO findById(String idEvento) {
        return toDTO(buscar(idEvento));
    }

    /* Solo se edita mientras esta en BORRADOR (despues de publicado ya se venden entradas). */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.EVENTOS, key = "#idEvento"),
            @CacheEvict(value = CacheConfig.BUSQUEDAS, allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.EVENTOS, key = "#idEvento"),
            @CacheEvict(value = CacheConfig.BUSQUEDAS, allEntries = true)
    })
    public void delete(String idEvento) {
        Evento evento = buscar(idEvento);
        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new IllegalStateException("Solo se puede borrar un evento en BORRADOR; si ya se publico, hay que cancelarlo");
        }
        repository.delete(evento);
    }

    /*-------------------------- BUSQUEDA (punto 9) --------------------------*/
    /* Solo muestra eventos a la venta o agotados (no borradores, cancelados ni finalizados).
     * Los filtros se arman en Mongo; el orden por precio y relevancia se calcula en memoria
     * porque dependen de varios campos (precio minimo de los sectores, coincidencias de texto). */
    @Cacheable(value = CacheConfig.BUSQUEDAS, key = "#filtro")
    public List<EventoResponseDTO> buscar(BusquedaEventoDTO filtro) {
        Query query = new Query(Criteria.where("estado").in(EstadoEvento.PUBLICADO, EstadoEvento.AGOTADO));

        if (filtro.tipo() != null) {
            query.addCriteria(Criteria.where("tipo").is(filtro.tipo()));
        }
        if (filtro.idOrganizador() != null) {
            query.addCriteria(Criteria.where("idOrganizador").is(filtro.idOrganizador()));
        }
        if (filtro.participante() != null) {
            query.addCriteria(Criteria.where("participantes").regex(contiene(filtro.participante()), "i"));
        }
        if (filtro.ciudad() != null) {
            List<String> idsLugares = lugarService.idsPorCiudad(filtro.ciudad());
            if (idsLugares.isEmpty()) return List.of();
            query.addCriteria(Criteria.where("idLugar").in(idsLugares));
        }
        if (filtro.desde() != null || filtro.hasta() != null) {
            Criteria fecha = Criteria.where("fechaHora");
            if (filtro.desde() != null) fecha = fecha.gte(filtro.desde().atStartOfDay());
            if (filtro.hasta() != null) fecha = fecha.lt(filtro.hasta().plusDays(1).atStartOfDay());
            query.addCriteria(fecha);
        }
        if (filtro.texto() != null) {
            String patron = contiene(filtro.texto());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("nombre").regex(patron, "i"),
                    Criteria.where("descripcion").regex(patron, "i"),
                    Criteria.where("participantes").regex(patron, "i")));
        }

        List<Evento> eventos = new ArrayList<>(mongoTemplate.find(query, Evento.class));
        eventos.sort(comparador(filtro));

        Map<String, String> nombresLugares = lugarService.nombresPorId(
                eventos.stream().map(Evento::getIdLugar).collect(Collectors.toSet()));
        return eventos.stream()
                .map(e -> mapper.convertToDTO(e, nombresLugares.get(e.getIdLugar())))
                .toList();
    }

    private Comparator<Evento> comparador(BusquedaEventoDTO filtro) {
        Comparator<Evento> porFecha = Comparator.comparing(Evento::getFechaHora);
        return switch (filtro.orden()) {
            case FECHA -> porFecha;
            // mas barato primero (precio minimo entre sus sectores); empate -> el mas proximo
            case PRECIO -> Comparator.comparingDouble(this::precioMinimo).thenComparing(porFecha);
            // mas coincidencias primero; empate -> el mas proximo
            case RELEVANCIA -> Comparator.comparingInt((Evento e) -> puntaje(e, filtro)).reversed().thenComparing(porFecha);
        };
    }

    private double precioMinimo(Evento evento) {
        return evento.getPrecios().stream()
                .mapToDouble(PrecioSector::getPrecio)
                .min()
                .orElse(Double.MAX_VALUE);
    }

    /* Relevancia: suma puntos por cada coincidencia con lo que busco el cliente.
     * Nombre pesa mas que participantes, y participantes mas que la descripcion. */
    private int puntaje(Evento evento, BusquedaEventoDTO filtro) {
        int puntos = 0;
        String texto = filtro.texto() == null ? null : filtro.texto().toLowerCase();
        String participante = filtro.participante() == null ? null : filtro.participante().toLowerCase();
        if (texto != null) {
            if (incluye(evento.getNombre(), texto)) puntos += 3;
            if (evento.getParticipantes().stream().anyMatch(p -> incluye(p, texto))) puntos += 2;
            if (incluye(evento.getDescripcion(), texto)) puntos += 1;
        }
        if (participante != null && evento.getParticipantes().stream()
                .anyMatch(p -> p.equalsIgnoreCase(participante))) {
            puntos += 2; // coincidencia exacta del artista/equipo
        }
        return puntos;
    }

    private boolean incluye(String campo, String buscado) {
        return campo != null && campo.toLowerCase().contains(buscado);
    }

    /* Escapa el texto del usuario para que no se interprete como expresion regular. */
    private String contiene(String texto) {
        return Pattern.quote(texto);
    }

    /*-------------------------- CAMBIO DE ESTADO --------------------------*/
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.EVENTOS, key = "#idEvento"),
            @CacheEvict(value = CacheConfig.BUSQUEDAS, allEntries = true)
    })
    public EventoResponseDTO cambiarEstado(String idEvento, EstadoEvento nuevo, Long usuarioId) {
        ResponseEntity<UsuarioResponseDTO> response = usuarioFeingClient.getById(usuarioId);
        UsuarioResponseDTO usuario = response.getBody();
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        Evento evento = buscar(idEvento);
        EstadoEvento actual = evento.getEstado();
        boolean esAdmin = (usuario.getRol() == Rol.ADMINISTRADOR);
        boolean esOrganizadorDueño = (usuario.getRol() == Rol.ORGANIZADOR && evento.getIdOrganizador().equals(usuarioId));
        if (nuevo == EstadoEvento.CANCELADO || nuevo == EstadoEvento.SUSPENDIDO) {
            if (!esAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para cancelar este evento");
            }
        } else {
            if (!esOrganizadorDueño && !esAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar este evento");
            }
        }
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
            case PUBLICADO -> nuevo == EstadoEvento.AGOTADO || nuevo == EstadoEvento.FINALIZADO
                    || nuevo == EstadoEvento.CANCELADO || nuevo == EstadoEvento.SUSPENDIDO;
            case AGOTADO -> nuevo == EstadoEvento.PUBLICADO || nuevo == EstadoEvento.FINALIZADO
                    || nuevo == EstadoEvento.CANCELADO || nuevo == EstadoEvento.SUSPENDIDO;
            case SUSPENDIDO -> nuevo == EstadoEvento.PUBLICADO || nuevo == EstadoEvento.CANCELADO || nuevo == EstadoEvento.FINALIZADO;
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
