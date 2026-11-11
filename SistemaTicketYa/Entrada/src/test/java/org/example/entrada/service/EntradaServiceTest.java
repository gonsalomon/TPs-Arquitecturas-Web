package org.example.entrada.service;

import org.example.entrada.client.PagoFeignClient;
import org.example.entrada.client.ReservaFeignClient;
import org.example.entrada.client.UsuarioFeignClient;
import org.example.entrada.client.usuario.response.dto.UsuarioResponseDTO;
import org.example.entrada.client.utils.Rol;
import org.example.entrada.dto.CambioEstadoRequest;
import org.example.entrada.dto.EmitirEntradasRequest;
import org.example.entrada.dto.EntradaDTO;
import org.example.entrada.dto.ItemEntradaRequest;
import org.example.entrada.dto.RevisionEntradaRequestDTO;
import org.example.entrada.dto.response.EntradaEmitidaDTO;
import org.example.entrada.entity.Entrada;
import org.example.entrada.exception.AsientoNoDisponibleException;
import org.example.entrada.exception.DevolucionFueraDePlazoException;
import org.example.entrada.exception.LimiteEntradasExcedidoException;
import org.example.entrada.mapper.EntradaMapper;
import org.example.entrada.repository.EntradaRepository;
import org.example.entrada.utils.EstadoEntrada;
import org.example.entrada.utils.EstadoEvento;
import org.example.entrada.utils.MotivoCambio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntradaServiceTest {

    private static final int MAX_POR_CLIENTE = 6;

    @Mock private EntradaRepository repo;
    @Mock private UsuarioFeignClient usuarioClient;
    @Mock private PagoFeignClient pagoClient;
    @Mock private ReservaFeignClient reservaClient;
    @Captor private ArgumentCaptor<List<Entrada>> guardadas;

    private EntradaService service;

    @BeforeEach
    void setUp() {
        service = new EntradaService(repo, new EntradaMapper(), MAX_POR_CLIENTE, usuarioClient, pagoClient, reservaClient);
    }

    // ---------------------------------------------------------------- helpers

    private ItemEntradaRequest item(Integer fila, Integer asiento) {
        return new ItemEntradaRequest("Platea", fila, asiento, new BigDecimal("1500.00"));
    }

    private EmitirEntradasRequest pedido(ItemEntradaRequest... items) {
        return new EmitirEntradasRequest(100L, "evt-1", 7L, LocalDateTime.now().plusDays(30), List.of(items));
    }

    private Entrada entrada(LocalDateTime fechaEvento) {
        Entrada e = new Entrada();
        e.setEventoId("evt-1");
        e.setSectorId("Platea");
        e.setClienteId(7L);
        e.setOrdenId(100L);
        e.setFila(1);
        e.setAsiento(1);
        e.setPrecio(new BigDecimal("1500.00"));
        e.setFechaEvento(fechaEvento);
        e.setClaveIdempotencia("100-0");
        return e;
    }

    private Entrada entrada() {
        return entrada(LocalDateTime.now().plusDays(30));
    }

    // ---------------------------------------------------------------- emitir

    @Test
    void emitirGuardaTodasLasEntradasConSuClaveDeIdempotencia() {
        when(repo.countByClienteIdAndEventoIdAndEstadoIn(eq(7L), eq("evt-1"), anyCollection())).thenReturn(0L);
        when(repo.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<EntradaEmitidaDTO> dtos = service.emitir(pedido(item(1, 1), item(1, 2)));

        assertEquals(2, dtos.size());
        verify(repo).saveAllAndFlush(guardadas.capture());
        List<Entrada> g = guardadas.getValue();
        assertEquals("100-0", g.get(0).getClaveIdempotencia());
        assertEquals("100-1", g.get(1).getClaveIdempotencia());
        assertEquals(EstadoEntrada.VALIDA, g.get(0).getEstado());
        assertEquals(7L, g.get(0).getClienteId());
        assertEquals("evt-1", g.get(0).getEventoId());
    }

    @Test
    void emitirEsIdempotenteSiLaOrdenYaTieneEntradas() {
        when(repo.findByOrdenId(100L)).thenReturn(List.of(entrada()));

        List<EntradaEmitidaDTO> dtos = service.emitir(pedido(item(1, 1)));

        assertEquals(1, dtos.size());
        verify(repo, never()).saveAllAndFlush(anyList());
        verify(repo, never()).countByClienteIdAndEventoIdAndEstadoIn(any(), any(), any());
    }

    @Test
    void emitirRechazaFilaSinAsiento() {
        assertThrows(IllegalArgumentException.class, () -> service.emitir(pedido(item(1, null))));
        assertThrows(IllegalArgumentException.class, () -> service.emitir(pedido(item(null, 1))));
        verify(repo, never()).saveAllAndFlush(anyList());
    }

    @Test
    void emitirAceptaZonaSinFilaNiAsiento() {
        when(repo.countByClienteIdAndEventoIdAndEstadoIn(any(), any(), anyCollection())).thenReturn(0L);
        when(repo.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<EntradaEmitidaDTO> dtos = service.emitir(pedido(item(null, null)));

        assertNull(dtos.get(0).getFila());
        assertNull(dtos.get(0).getAsiento());
    }

    @Test
    void emitirRechazaSiSuperaElLimitePorCliente() {
        when(repo.countByClienteIdAndEventoIdAndEstadoIn(any(), any(), anyCollection())).thenReturn(5L);

        assertThrows(LimiteEntradasExcedidoException.class, () -> service.emitir(pedido(item(1, 1), item(1, 2))));
        verify(repo, never()).saveAllAndFlush(anyList());
    }

    @Test
    void emitirPermiteLlegarJustoAlLimite() {
        when(repo.countByClienteIdAndEventoIdAndEstadoIn(any(), any(), anyCollection())).thenReturn(4L);
        when(repo.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(2, service.emitir(pedido(item(1, 1), item(1, 2))).size());
    }

    @Test
    void emitirFallaSiUnAsientoYaFueVendido() {
        when(repo.countByClienteIdAndEventoIdAndEstadoIn(any(), any(), anyCollection())).thenReturn(0L);
        when(repo.saveAllAndFlush(anyList())).thenThrow(new DataIntegrityViolationException("unique"));
        // findByOrdenId devuelve vacío en las dos llamadas: otra orden tiene el asiento

        assertThrows(AsientoNoDisponibleException.class, () -> service.emitir(pedido(item(1, 1))));
    }

    @Test
    void emitirDevuelveLasExistentesSiUnReintentoConcurrenteGano() {
        when(repo.findByOrdenId(100L)).thenReturn(new ArrayList<>()).thenReturn(List.of(entrada()));
        when(repo.countByClienteIdAndEventoIdAndEstadoIn(any(), any(), anyCollection())).thenReturn(0L);
        when(repo.saveAllAndFlush(anyList())).thenThrow(new DataIntegrityViolationException("unique"));

        List<EntradaEmitidaDTO> dtos = service.emitir(pedido(item(1, 1)));

        assertEquals(1, dtos.size());
    }

    // ---------------------------------------------------------------- consultas

    @Test
    void obtenerPorIdDevuelveLaEntrada() {
        when(repo.findById(1L)).thenReturn(Optional.of(entrada()));

        EntradaEmitidaDTO dto = service.obtenerPorId(1L);
        assertEquals(1, dto.getFila());
        assertEquals(1, dto.getAsiento());
        assertNotNull(dto.getCodigoQr());
    }

    @Test
    void obtenerPorIdInexistenteEs404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.obtenerPorId(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void buscarSinFiltrosEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.buscar(null, null));
        assertThrows(IllegalArgumentException.class, () -> service.buscar(null, "  "));
    }

    @Test
    void buscarPorClienteYEvento() {
        when(repo.findByClienteIdAndEventoId(7L, "evt-1")).thenReturn(List.of(entrada()));

        assertEquals(1, service.buscar(7L, "evt-1").size());
        verify(repo, never()).findByClienteId(any());
        verify(repo, never()).findByEventoId(any());
    }

    @Test
    void buscarSoloPorCliente() {
        when(repo.findByClienteId(7L)).thenReturn(List.of(entrada()));

        assertEquals(1, service.buscar(7L, null).size());
        assertEquals(1, service.buscar(7L, " ").size());
    }

    @Test
    void buscarSoloPorEvento() {
        when(repo.findByEventoId("evt-1")).thenReturn(List.of(entrada()));

        assertEquals(1, service.buscar(null, "evt-1").size());
    }

    @Test
    void contarActivasCuentaSoloValidasYUsadas() {
        when(repo.countByClienteIdAndEventoIdAndEstadoIn(
                7L, "evt-1", List.of(EstadoEntrada.VALIDA, EstadoEntrada.USADA))).thenReturn(3L);

        assertEquals(3L, service.contarActivas(7L, "evt-1"));
    }

    // ---------------------------------------------------------------- cambiarEstado

    @Test
    void cambiarEstadoMarcaLaEntradaComoUsada() {
        Entrada e = entrada();
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        EntradaDTO dto = service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.USADA, null));

        assertEquals(EstadoEntrada.USADA, e.getEstado());
        assertEquals(EstadoEntrada.USADA, dto.estado());
    }

    @Test
    void cambiarEstadoALaMismoEstadoEsIdempotente() {
        Entrada e = entrada();
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.VALIDA, null));

        assertEquals(EstadoEntrada.VALIDA, e.getEstado());
    }

    @Test
    void reintegroRepetidoNoFallaAunSinMotivo() {
        Entrada e = entrada();
        e.cambiarEstado(EstadoEntrada.REINTEGRO);
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        assertDoesNotThrow(() -> service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, null)));
    }

    @Test
    void reintegroSinMotivoEsInvalido() {
        Entrada e = entrada();
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, null)));
        assertEquals(EstadoEntrada.VALIDA, e.getEstado());
    }

    @Test
    void devolucionDentroDePlazoPasaAReintegro() {
        Entrada e = entrada(LocalDateTime.now().plusDays(5));
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION));

        assertEquals(EstadoEntrada.REINTEGRO, e.getEstado());
    }

    @Test
    void devolucionLiberaLaClaveDelAsiento() {
        Entrada e = entrada(LocalDateTime.now().plusDays(5));
        e.cambiarEstado(EstadoEntrada.VALIDA);                 // calcula la clave, como lo haría @PrePersist
        assertNotNull(e.getClaveAsientoActivo());
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION));

        assertNull(e.getClaveAsientoActivo());                 // el asiento puede venderse de nuevo
    }

    @Test
    void marcarComoUsadaMantieneElAsientoOcupado() {
        Entrada e = entrada();
        e.cambiarEstado(EstadoEntrada.VALIDA);
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.USADA, null));

        assertNotNull(e.getClaveAsientoActivo());
    }

    @Test
    void devolucionFueraDePlazoSeRechaza() {
        Entrada e = entrada(LocalDateTime.now().plusHours(10));   // faltan menos de 72 h
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(DevolucionFueraDePlazoException.class,
                () -> service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION)));
        assertEquals(EstadoEntrada.VALIDA, e.getEstado());
    }

    @Test
    void transicionInvalidaLanzaIllegalState() {
        Entrada e = entrada();
        e.cambiarEstado(EstadoEntrada.REINTEGRO);
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.USADA, null)));
    }

    @Test
    void unaEntradaUsadaNoSePuedeReintegrar() {
        Entrada e = entrada();
        e.cambiarEstado(EstadoEntrada.USADA);
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION)));
        assertEquals(EstadoEntrada.USADA, e.getEstado());
    }

    // ---------------------------------------------------------------- reintegros masivos

    @Test
    void reintegroPorEventoPasaTodasLasValidasAReintegroSinChequearPlazo() {
        Entrada a = entrada(LocalDateTime.now().plusHours(1));    // dentro de las 72 h: igual se reintegra
        Entrada b = entrada(LocalDateTime.now().plusHours(1));
        when(repo.findByEventoIdAndEstado("evt-1", EstadoEntrada.VALIDA)).thenReturn(List.of(a, b));

        List<EntradaEmitidaDTO> dtos = service.reintegroPorEvento("evt-1");

        assertEquals(2, dtos.size());
        assertEquals(EstadoEntrada.REINTEGRO, a.getEstado());
        assertEquals(EstadoEntrada.REINTEGRO, b.getEstado());
    }

    @Test
    void reintegroPorEventoSinEntradasDevuelveListaVacia() {
        when(repo.findByEventoIdAndEstado("evt-1", EstadoEntrada.VALIDA)).thenReturn(List.of());

        assertTrue(service.reintegroPorEvento("evt-1").isEmpty());
    }

    @Test
    void reintegroPorOrdenPasaLasValidasAReintegro() {
        Entrada a = entrada(LocalDateTime.now().plusHours(1));
        when(repo.findByOrdenIdAndEstado(100L, EstadoEntrada.VALIDA)).thenReturn(List.of(a));

        List<EntradaEmitidaDTO> dtos = service.reintegroPorOrden(100L);

        assertEquals(1, dtos.size());
        assertEquals(EstadoEntrada.REINTEGRO, a.getEstado());
    }

    // ---------------------------------------------------------------- revisión de devoluciones (admin)

    private void usuarioConRol(Long id, Rol rol) {
        when(usuarioClient.getById(id)).thenReturn(ResponseEntity.ok(new UsuarioResponseDTO(id, "N", "A", "x@mail.com", rol)));
    }

    private Entrada entradaValida(LocalDateTime fechaEvento) {
        Entrada e = entrada(fechaEvento);
        e.cambiarEstado(EstadoEntrada.VALIDA);                 // calcula la clave del asiento
        return e;
    }

    @Test
    void aprobarLaDevolucionAnulaLaEntradaLiberaElAsientoYReembolsa() {
        usuarioConRol(1L, Rol.ADMINISTRADOR);
        Entrada e = entradaValida(LocalDateTime.now().plusDays(5));
        when(repo.findById(50L)).thenReturn(Optional.of(e));

        service.revisarSolicitud(new RevisionEntradaRequestDTO(EstadoEvento.APROBADO, 12L, 50L), 1L);

        assertEquals(EstadoEntrada.REINTEGRO, e.getEstado());
        assertNull(e.getClaveAsientoActivo());                 // el asiento se puede volver a vender
        InOrder orden = inOrder(repo, pagoClient, reservaClient);
        orden.verify(repo).save(e);                            // primero se guarda la entrada anulada
        orden.verify(pagoClient).reembolsarPago(12L);          // después se devuelve la plata
        orden.verify(reservaClient).actualizarEstadoReserva(eq(12L), anyString());
    }

    @Test
    void aprobarFueraDePlazoNoReembolsaNiLiberaNada() {
        usuarioConRol(1L, Rol.ADMINISTRADOR);
        Entrada e = entradaValida(LocalDateTime.now().plusHours(71));
        when(repo.findById(50L)).thenReturn(Optional.of(e));

        assertThrows(DevolucionFueraDePlazoException.class,
                () -> service.revisarSolicitud(new RevisionEntradaRequestDTO(EstadoEvento.APROBADO, 12L, 50L), 1L));

        assertEquals(EstadoEntrada.VALIDA, e.getEstado());
        verify(repo, never()).save(any());
        verifyNoInteractions(pagoClient, reservaClient);
    }

    @Test
    void aprobarSinIndicarLaEntradaEsInvalido() {
        usuarioConRol(1L, Rol.ADMINISTRADOR);

        assertThrows(IllegalArgumentException.class,
                () -> service.revisarSolicitud(new RevisionEntradaRequestDTO(EstadoEvento.APROBADO, 12L, null), 1L));
        verifyNoInteractions(pagoClient, reservaClient);
    }

    @Test
    void siSeRechazaLaDevolucionNoSeTocaNada() {
        usuarioConRol(1L, Rol.ADMINISTRADOR);

        service.revisarSolicitud(new RevisionEntradaRequestDTO(EstadoEvento.RECHAZADO, 12L, 50L), 1L);

        verify(repo, never()).save(any());
        verifyNoInteractions(pagoClient, reservaClient);
    }

    @Test
    void soloUnAdministradorPuedeRevisarDevoluciones() {
        usuarioConRol(7L, Rol.CLIENTE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.revisarSolicitud(new RevisionEntradaRequestDTO(EstadoEvento.APROBADO, 12L, 50L), 7L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verifyNoInteractions(pagoClient, reservaClient);
    }

    @Test
    void unUsuarioInexistenteNoPuedeRevisarDevoluciones() {
        when(usuarioClient.getById(99L)).thenReturn(ResponseEntity.ok(null));

        assertThrows(IllegalArgumentException.class,
                () -> service.revisarSolicitud(new RevisionEntradaRequestDTO(EstadoEvento.APROBADO, 12L, 50L), 99L));
        verifyNoInteractions(pagoClient, reservaClient);
    }
}
