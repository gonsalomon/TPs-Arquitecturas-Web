package org.example.evento.service;

import org.example.evento.client.UsuarioFeingClient;
import org.example.evento.client.usuario.dto.response.UsuarioResponseDTO;
import org.example.evento.client.utils.Rol;
import org.example.evento.dto.request.BusquedaEventoDTO;
import org.example.evento.dto.request.EventoRequestDTO;
import org.example.evento.dto.request.PrecioSectorDTO;
import org.example.evento.dto.response.EventoResponseDTO;
import org.example.evento.entity.Evento;
import org.example.evento.entity.Lugar;
import org.example.evento.entity.PrecioSector;
import org.example.evento.entity.Sector;
import org.example.evento.mapper.EventoMapper;
import org.example.evento.repository.EventoRepository;
import org.example.evento.utils.EstadoEvento;
import org.example.evento.utils.OrdenBusqueda;
import org.example.evento.utils.TipoEvento;
import org.example.evento.utils.TipoSector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    private static final Long ORGANIZADOR = 1L;
    private static final Long ADMIN = 2L;
    private static final Long CLIENTE = 3L;

    @Mock private EventoRepository repository;
    @Mock private LugarService lugarService;
    @Mock private MongoTemplate mongoTemplate;
    @Mock private UsuarioFeingClient usuarioClient;

    private EventoService service;

    @BeforeEach
    void setUp() {
        service = new EventoService(repository, lugarService, new EventoMapper(), mongoTemplate, usuarioClient);
        lenient().when(repository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(lugarService.buscar("lugar-1")).thenReturn(estadio());
        lenient().when(usuarioClient.getById(ORGANIZADOR)).thenReturn(ResponseEntity.ok(usuario(ORGANIZADOR, Rol.ORGANIZADOR)));
        lenient().when(usuarioClient.getById(ADMIN)).thenReturn(ResponseEntity.ok(usuario(ADMIN, Rol.ADMINISTRADOR)));
        lenient().when(usuarioClient.getById(CLIENTE)).thenReturn(ResponseEntity.ok(usuario(CLIENTE, Rol.CLIENTE)));
    }

    private UsuarioResponseDTO usuario(Long id, Rol rol) {
        return new UsuarioResponseDTO(id, "Nombre", "Apellido", "mail@x.com", rol);
    }

    private Lugar estadio() {
        Lugar lugar = new Lugar("Estadio Tandil", "Calle 1", "Tandil", -37.3, -59.1, List.of(
                new Sector("Campo", TipoSector.NO_NUMERADO, null, null, 5000),
                new Sector("Platea", TipoSector.NUMERADO, 20, 30, null)));
        lugar.setIdLugar("lugar-1");
        return lugar;
    }

    private EventoRequestDTO request(List<PrecioSectorDTO> precios) {
        EventoRequestDTO r = new EventoRequestDTO();
        r.setNombre("Recital X");
        r.setTipo(TipoEvento.RECITAL);
        r.setParticipantes(List.of("Banda X"));
        r.setFechaHora(LocalDateTime.now().plusMonths(1));
        r.setIdLugar("lugar-1");
        r.setPrecios(precios);
        return r;
    }

    private Evento evento(String id, EstadoEvento estado, List<PrecioSector> precios) {
        Evento e = new Evento();
        e.setIdEvento(id);
        e.setNombre("Evento " + id);
        e.setIdOrganizador(ORGANIZADOR);
        e.setIdLugar("lugar-1");
        e.setFechaHora(LocalDateTime.now().plusMonths(1));
        e.setEstado(estado);
        e.setPrecios(new ArrayList<>(precios));
        return e;
    }

    private List<PrecioSector> preciosCompletos() {
        return List.of(new PrecioSector("Campo", 30000.0), new PrecioSector("Platea", 45000.0));
    }

    // ---------- Crear ----------

    @Test
    void unOrganizadorCreaElEventoEnBorrador() {
        EventoResponseDTO dto = service.save(request(List.of(new PrecioSectorDTO("Campo", 30000.0))), ORGANIZADOR);

        assertEquals(EstadoEvento.BORRADOR, dto.getEstado());
        assertEquals(ORGANIZADOR, dto.getIdOrganizador());
        assertEquals("Estadio Tandil", dto.getNombreLugar());
    }

    @Test
    void unClienteNoPuedeCrearEventos() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.save(request(List.of()), CLIENTE));
        assertEquals(403, ex.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void noSePuedePonerPrecioAUnSectorQueNoExisteEnElLugar() {
        assertThrows(IllegalArgumentException.class,
                () -> service.save(request(List.of(new PrecioSectorDTO("Palco VIP", 90000.0))), ORGANIZADOR));
    }

    @Test
    void noSePuedeRepetirElPrecioDeUnSector() {
        assertThrows(IllegalArgumentException.class, () -> service.save(request(List.of(
                new PrecioSectorDTO("Campo", 30000.0), new PrecioSectorDTO("campo", 20000.0))), ORGANIZADOR));
    }

    // ---------- Editar / borrar ----------

    @Test
    void unEventoPublicadoNoSePuedeEditarNiBorrar() {
        when(repository.findById("e1")).thenReturn(Optional.of(evento("e1", EstadoEvento.PUBLICADO, preciosCompletos())));

        assertThrows(IllegalStateException.class, () -> service.update("e1", request(List.of())));
        assertThrows(IllegalStateException.class, () -> service.delete("e1"));
        verify(repository, never()).delete(any());
    }

    // ---------- Cambio de estado ----------

    @Test
    void elOrganizadorPublicaSuEventoSiTodosLosSectoresTienenPrecio() {
        when(repository.findById("e1")).thenReturn(Optional.of(evento("e1", EstadoEvento.BORRADOR, preciosCompletos())));

        EventoResponseDTO dto = service.cambiarEstado("e1", EstadoEvento.PUBLICADO, ORGANIZADOR);

        assertEquals(EstadoEvento.PUBLICADO, dto.getEstado());
    }

    @Test
    void noSePublicaSiFaltaElPrecioDeAlgunSector() {
        when(repository.findById("e1")).thenReturn(Optional.of(
                evento("e1", EstadoEvento.BORRADOR, List.of(new PrecioSector("Campo", 30000.0)))));

        assertThrows(IllegalStateException.class, () -> service.cambiarEstado("e1", EstadoEvento.PUBLICADO, ORGANIZADOR));
    }

    @Test
    void soloUnAdministradorPuedeCancelar() {
        when(repository.findById("e1")).thenReturn(Optional.of(evento("e1", EstadoEvento.PUBLICADO, preciosCompletos())));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado("e1", EstadoEvento.CANCELADO, ORGANIZADOR));
        assertEquals(403, ex.getStatusCode().value());

        assertEquals(EstadoEvento.CANCELADO, service.cambiarEstado("e1", EstadoEvento.CANCELADO, ADMIN).getEstado());
    }

    @Test
    void unAdministradorPuedeSuspenderUnEventoALaVentaYDespuesReanudarlo() {
        Evento e = evento("e1", EstadoEvento.PUBLICADO, preciosCompletos());
        when(repository.findById("e1")).thenReturn(Optional.of(e));

        assertEquals(EstadoEvento.SUSPENDIDO, service.cambiarEstado("e1", EstadoEvento.SUSPENDIDO, ADMIN).getEstado());
        assertEquals(EstadoEvento.PUBLICADO, service.cambiarEstado("e1", EstadoEvento.PUBLICADO, ADMIN).getEstado());
    }

    @Test
    void unEventoCanceladoNoVuelveAPublicarse() {
        when(repository.findById("e1")).thenReturn(Optional.of(evento("e1", EstadoEvento.CANCELADO, preciosCompletos())));

        assertThrows(IllegalStateException.class, () -> service.cambiarEstado("e1", EstadoEvento.PUBLICADO, ADMIN));
    }

    @Test
    void otroOrganizadorNoPuedePublicarUnEventoAjeno() {
        when(usuarioClient.getById(9L)).thenReturn(ResponseEntity.ok(usuario(9L, Rol.ORGANIZADOR)));
        when(repository.findById("e1")).thenReturn(Optional.of(evento("e1", EstadoEvento.BORRADOR, preciosCompletos())));

        assertThrows(ResponseStatusException.class, () -> service.cambiarEstado("e1", EstadoEvento.PUBLICADO, 9L));
    }

    // ---------- Búsqueda ----------

    @Test
    void laBusquedaOrdenaPorPrecioMinimoDelEvento() {
        Evento caro = evento("caro", EstadoEvento.PUBLICADO, List.of(new PrecioSector("Campo", 50000.0)));
        Evento barato = evento("barato", EstadoEvento.PUBLICADO, List.of(new PrecioSector("Campo", 10000.0)));
        when(mongoTemplate.find(any(Query.class), eq(Evento.class))).thenReturn(new ArrayList<>(List.of(caro, barato)));
        when(lugarService.nombresPorId(anySet())).thenReturn(Map.of("lugar-1", "Estadio Tandil"));

        List<EventoResponseDTO> r = service.buscar(
                new BusquedaEventoDTO(null, null, null, null, null, null, null, OrdenBusqueda.PRECIO));

        assertEquals(List.of("barato", "caro"), r.stream().map(EventoResponseDTO::getIdEvento).toList());
    }

    @Test
    void siNingunLugarEstaEnLaCiudadNoConsultaEventos() {
        when(lugarService.idsPorCiudad("Azul")).thenReturn(List.of());

        List<EventoResponseDTO> r = service.buscar(
                new BusquedaEventoDTO(null, null, null, "Azul", null, null, null, null));

        assertTrue(r.isEmpty());
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void elRangoDeFechasNoPuedeEstarInvertido() {
        assertThrows(IllegalArgumentException.class, () -> new BusquedaEventoDTO(null, null, null, null,
                java.time.LocalDate.of(2026, 12, 31), java.time.LocalDate.of(2026, 12, 1), null, null));
    }
}
