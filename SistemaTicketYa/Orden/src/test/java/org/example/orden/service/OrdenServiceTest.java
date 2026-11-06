package org.example.orden.service;

import feign.FeignException;
import org.example.orden.dto.request.AsientoRequestDTO;
import org.example.orden.dto.request.CrearOrdenRequestDTO;
import org.example.orden.dto.request.PagarOrdenRequestDTO;
import org.example.orden.dto.response.*;
import org.example.orden.entity.ItemOrden;
import org.example.orden.entity.Orden;
import org.example.orden.exception.*;
import org.example.orden.feignClients.EntradaClient;
import org.example.orden.feignClients.EventoClient;
import org.example.orden.feignClients.PagoClient;
import org.example.orden.feignClients.ReservaClient;
import org.example.orden.mapper.OrdenMapper;
import org.example.orden.repository.OrdenRepository;
import org.example.orden.utils.EstadoOrden;
import org.example.orden.utils.MedioPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private EventoClient eventoClient;
    @Mock
    private ReservaClient reservaClient;
    @Mock
    private PagoClient pagoClient;
    @Mock
    private EntradaClient entradaClient;

    private OrdenService service;

    @BeforeEach
    void setUp() {
        service = new OrdenService(ordenRepository, new OrdenMapper(), eventoClient, reservaClient, pagoClient, entradaClient);
        ReflectionTestUtils.setField(service, "limiteEntradasPorEvento", 6);
        lenient().when(ordenRepository.saveAndFlush(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private EventoExternoDTO eventoPublicado() {
        EventoExternoDTO evento = new EventoExternoDTO();
        evento.setIdEvento("evt-1");
        evento.setEstado("PUBLICADO");
        evento.setIdLugar("lugar-1");
        evento.setFechaHora(LocalDateTime.of(2026, 12, 10, 21, 0));
        evento.setPrecios(List.of(new PrecioSectorExternoDTO("Platea", 15000.0)));
        return evento;
    }

    private LugarExternoDTO lugarConSectorNumerado() {
        SectorExternoDTO sector = new SectorExternoDTO("Platea", "NUMERADO", 10, 20, 200);
        return new LugarExternoDTO(List.of(sector));
    }

    private LugarExternoDTO lugarConSectorNoNumerado() {
        SectorExternoDTO sector = new SectorExternoDTO("Campo", "NO_NUMERADO", null, null, 500);
        return new LugarExternoDTO(List.of(sector));
    }

    private CrearOrdenRequestDTO requestDosAsientos() {
        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO();
        request.setEventoId("evt-1");
        request.setSector("Platea");
        request.setAsientos(List.of(new AsientoRequestDTO(3, 5), new AsientoRequestDTO(3, 6)));
        return request;
    }

    // ---------- Crear orden ----------

    @Test
    void creaUnaOrdenParaAsientosNumeradosDisponibles() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());
        when(ordenRepository.contarEntradasActivasPorClienteYEvento(7L, "evt-1")).thenReturn(0L);
        when(reservaClient.crearReserva(any())).thenReturn(
                new ReservaExternaDTO(31L, 7L, "evt-1", "Platea", List.of(45L, 46L), "PENDIENTE",
                        LocalDateTime.now(), LocalDateTime.now().plusMinutes(10)));

        OrdenResponseDTO resultado = service.crear(7L, requestDosAsientos());

        assertEquals(EstadoOrden.PENDIENTE_PAGO.name(), resultado.getEstado());
        assertEquals(new BigDecimal("30000.0"), resultado.getTotal());
        assertEquals(2, resultado.getItems().size());
    }

    @Test
    void creaUnaOrdenParaSectorNoNumeradoIndicandoCantidad() {
        EventoExternoDTO evento = eventoPublicado();
        evento.setPrecios(List.of(new PrecioSectorExternoDTO("Campo", 8000.0)));
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(evento);
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNoNumerado());
        when(ordenRepository.contarEntradasActivasPorClienteYEvento(7L, "evt-1")).thenReturn(0L);
        when(reservaClient.crearReserva(any())).thenReturn(
                new ReservaExternaDTO(32L, 7L, "evt-1", "Campo", null, "PENDIENTE",
                        LocalDateTime.now(), LocalDateTime.now().plusMinutes(10)));

        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO();
        request.setEventoId("evt-1");
        request.setSector("Campo");
        request.setCantidad(3);

        OrdenResponseDTO resultado = service.crear(7L, request);

        assertEquals(EstadoOrden.PENDIENTE_PAGO.name(), resultado.getEstado());
        assertEquals(3, resultado.getItems().size());
        assertEquals(new BigDecimal("24000.0"), resultado.getTotal());
    }

    @Test
    void rechazaAsientosEnUnSectorNoNumerado() {
        EventoExternoDTO evento = eventoPublicado();
        evento.setPrecios(List.of(new PrecioSectorExternoDTO("Campo", 8000.0)));
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(evento);
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNoNumerado());

        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO();
        request.setEventoId("evt-1");
        request.setSector("Campo");
        request.setAsientos(List.of(new AsientoRequestDTO(1, 1)));

        assertThrows(IllegalArgumentException.class, () -> service.crear(7L, request));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void rechazaUnaCantidadEnUnSectorNumerado() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());

        CrearOrdenRequestDTO request = requestDosAsientos();
        request.setCantidad(2);

        assertThrows(IllegalArgumentException.class, () -> service.crear(7L, request));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void rechazaUnAsientoFueraDeRango() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());

        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO();
        request.setEventoId("evt-1");
        request.setSector("Platea");
        request.setAsientos(List.of(new AsientoRequestDTO(99, 1))); // sector tiene 10 filas

        assertThrows(IllegalArgumentException.class, () -> service.crear(7L, request));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void rechazaAsientosRepetidosEnElMismoPedido() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());

        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO();
        request.setEventoId("evt-1");
        request.setSector("Platea");
        request.setAsientos(List.of(new AsientoRequestDTO(3, 5), new AsientoRequestDTO(3, 5)));

        assertThrows(IllegalArgumentException.class, () -> service.crear(7L, request));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void rechazaUnSectorSinPrecioDefinidoParaElEvento() {
        EventoExternoDTO evento = eventoPublicado();
        evento.setPrecios(List.of(new PrecioSectorExternoDTO("Campo", 8000.0))); // "Platea" no tiene precio
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(evento);

        assertThrows(IllegalArgumentException.class, () -> service.crear(7L, requestDosAsientos()));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void rechazaUnSectorQueNoExisteEnElLugar() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNoNumerado()); // no tiene "Platea"

        assertThrows(IllegalArgumentException.class, () -> service.crear(7L, requestDosAsientos()));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void evitaVenderCuandoElEventoNoExiste() {
        when(eventoClient.obtenerEvento("evt-1"))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "no existe"));

        assertThrows(ResponseStatusException.class, () -> service.crear(7L, requestDosAsientos()));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void evitaVenderCuandoElEventoNoEstaPublicado() {
        EventoExternoDTO evento = eventoPublicado();
        evento.setEstado("BORRADOR");
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(evento);

        assertThrows(EventoNoDisponibleException.class, () -> service.crear(7L, requestDosAsientos()));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void elPrecioSeTomaDelEventoIgnorandoCualquierOtroDato() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());
        when(ordenRepository.contarEntradasActivasPorClienteYEvento(7L, "evt-1")).thenReturn(0L);
        when(reservaClient.crearReserva(any())).thenReturn(
                new ReservaExternaDTO(31L, 7L, "evt-1", "Platea", List.of(45L, 46L), "PENDIENTE",
                        LocalDateTime.now(), LocalDateTime.now().plusMinutes(10)));

        OrdenResponseDTO resultado = service.crear(7L, requestDosAsientos());

        assertEquals(new BigDecimal("15000.0"), resultado.getItems().get(0).getPrecioUnitario());
        assertEquals(new BigDecimal("30000.0"), resultado.getTotal());
    }

    @Test
    void permiteUnPedidoDentroDelLimite() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());
        when(ordenRepository.contarEntradasActivasPorClienteYEvento(7L, "evt-1")).thenReturn(4L);
        when(reservaClient.crearReserva(any())).thenReturn(
                new ReservaExternaDTO(31L, 7L, "evt-1", "Platea", List.of(45L, 46L), "PENDIENTE",
                        LocalDateTime.now(), LocalDateTime.now().plusMinutes(10)));

        assertDoesNotThrow(() -> service.crear(7L, requestDosAsientos()));
    }

    @Test
    void rechazaUnPedidoQueSuperaElLimite() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());
        when(ordenRepository.contarEntradasActivasPorClienteYEvento(7L, "evt-1")).thenReturn(5L);

        assertThrows(LimiteEntradasExcedidoException.class, () -> service.crear(7L, requestDosAsientos()));
        verifyNoInteractions(reservaClient);
    }

    @Test
    void lugaresNoDisponiblesRechazaLaOrdenSinQuedarActiva() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());
        when(ordenRepository.contarEntradasActivasPorClienteYEvento(7L, "evt-1")).thenReturn(0L);
        when(reservaClient.crearReserva(any())).thenThrow(new IllegalStateException("ocupados"));

        assertThrows(AsientoNoDisponibleException.class, () -> service.crear(7L, requestDosAsientos()));
    }

    @Test
    void servicioDeReservasCaidoDaServicioNoDisponible() {
        when(eventoClient.obtenerEvento("evt-1")).thenReturn(eventoPublicado());
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugarConSectorNumerado());
        when(ordenRepository.contarEntradasActivasPorClienteYEvento(7L, "evt-1")).thenReturn(0L);
        when(reservaClient.crearReserva(any())).thenThrow(mock(FeignException.class));

        assertThrows(ServicioNoDisponibleException.class, () -> service.crear(7L, requestDosAsientos()));
    }

    // ---------- Pagar orden ----------

    private Orden ordenPendiente() {
        Orden orden = new Orden(7L, "evt-1", "Platea", LocalDateTime.of(2026, 12, 10, 21, 0));
        orden.setIdOrden(1L);
        orden.agregarItem(new ItemOrden(3, 5, 45, new BigDecimal("15000")));
        orden.setIdReserva(31L);
        orden.setFechaVencimiento(LocalDateTime.now().plusMinutes(5));
        return orden;
    }

    @Test
    void pagoAprobadoEmiteLasEntradasYConfirmaLaOrden() {
        Orden orden = ordenPendiente();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(pagoClient.realizarPago(any())).thenReturn(
                new PagoExternoDTO(99L, 7L, 31L, "ACEPTADO", "TARJETA_CREDITO", LocalDateTime.now()));
        when(entradaClient.emitir(any())).thenReturn(
                List.of(new EntradaEmitidaDTO(1L, "qr-1", "Platea", 3, 5)));

        OrdenResponseDTO resultado = service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO));

        assertEquals(EstadoOrden.CONFIRMADA.name(), resultado.getEstado());
        assertEquals(1, resultado.getEntradas().size());
        verify(reservaClient).actualizarEstado(31L, "CONFIRMADA");
    }

    @Test
    void pagoRechazadoLiberaLaReservaYNoEmite() {
        Orden orden = ordenPendiente();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(pagoClient.realizarPago(any())).thenReturn(
                new PagoExternoDTO(99L, 7L, 31L, "RECHAZADO", "TARJETA_CREDITO", LocalDateTime.now()));

        OrdenResponseDTO resultado = service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO));

        assertEquals(EstadoOrden.RECHAZADA.name(), resultado.getEstado());
        verify(reservaClient).actualizarEstado(31L, "LIBERADA");
        verifyNoInteractions(entradaClient);
    }

    @Test
    void pagarUnaOrdenVencidaLaMarcaVencidaYNoCobra() {
        Orden orden = ordenPendiente();
        orden.setFechaVencimiento(LocalDateTime.now().minusMinutes(1));
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThrows(OrdenVencidaException.class,
                () -> service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO)));

        assertEquals(EstadoOrden.VENCIDA, orden.getEstado());
        verify(reservaClient).actualizarEstado(31L, "LIBERADA");
        verifyNoInteractions(pagoClient);
    }

    @Test
    void reintentarPagoDeUnaOrdenRechazadaNoLlamaAServiciosExternos() {
        Orden orden = ordenPendiente();
        orden.cambiarEstado(EstadoOrden.RECHAZADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        OrdenResponseDTO resultado = service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO));

        assertEquals(EstadoOrden.RECHAZADA.name(), resultado.getEstado());
        verifyNoInteractions(pagoClient, reservaClient, entradaClient);
    }

    @Test
    void reintentarPagoDeUnaOrdenConfirmadaNoVuelveACobrar() {
        Orden orden = ordenPendiente();
        orden.cambiarEstado(EstadoOrden.CONFIRMADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(entradaClient.emitir(any())).thenReturn(List.of(new EntradaEmitidaDTO(1L, "qr-1", "Platea", 3, 5)));

        OrdenResponseDTO resultado = service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO));

        assertEquals(EstadoOrden.CONFIRMADA.name(), resultado.getEstado());
        assertEquals(1, resultado.getEntradas().size());
        verifyNoInteractions(pagoClient);
    }

    @Test
    void reintentarPagoTrasFalloDeEmisionSoloReintentaLaEmision() {
        Orden orden = ordenPendiente();
        orden.cambiarEstado(EstadoOrden.PAGO_APROBADO);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(entradaClient.emitir(any())).thenReturn(List.of(new EntradaEmitidaDTO(1L, "qr-1", "Platea", 3, 5)));

        OrdenResponseDTO resultado = service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO));

        assertEquals(EstadoOrden.CONFIRMADA.name(), resultado.getEstado());
        verifyNoInteractions(pagoClient);
    }

    @Test
    void pasarelaCaidaDevuelveServicioNoDisponibleYNoReintentaElCobro() {
        Orden orden = ordenPendiente();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(pagoClient.realizarPago(any())).thenThrow(mock(FeignException.class));

        assertThrows(ServicioNoDisponibleException.class,
                () -> service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO)));

        assertEquals(EstadoOrden.PENDIENTE_PAGO, orden.getEstado());
        verify(pagoClient, times(1)).realizarPago(any());
    }

    @Test
    void dosPagosSimultaneosRevisaElEstadoActualSinDuplicarElCobro() {
        Orden orden = ordenPendiente();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(pagoClient.realizarPago(any())).thenReturn(
                new PagoExternoDTO(99L, 7L, 31L, "ACEPTADO", "TARJETA_CREDITO", LocalDateTime.now()));
        when(ordenRepository.save(any(Orden.class)))
                .thenThrow(new org.springframework.dao.OptimisticLockingFailureException("perdió la carrera"));

        OrdenResponseDTO resultado = service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO));

        assertNotNull(resultado);
        verify(ordenRepository, times(2)).findById(1L);
    }

    @Test
    void pagoYaRegistradoEnPagoSeTrataComoResultadoIncierto() {
        Orden orden = ordenPendiente();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(pagoClient.realizarPago(any())).thenThrow(new IllegalStateException("ya pagada"));

        assertThrows(ServicioNoDisponibleException.class,
                () -> service.pagar(7L, 1L, new PagarOrdenRequestDTO(MedioPago.TARJETA_CREDITO)));

        assertEquals(EstadoOrden.PENDIENTE_PAGO, orden.getEstado());
        verifyNoInteractions(entradaClient);
    }

    // ---------- Consultas ----------

    @Test
    void consultaUnaOrdenPropiaExistente() {
        Orden orden = ordenPendiente();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        OrdenResponseDTO resultado = service.buscarPorId(7L, 1L);

        assertEquals(1L, resultado.getId());
        assertEquals(EstadoOrden.PENDIENTE_PAGO.name(), resultado.getEstado());
    }

    @Test
    void listaSoloLasOrdenesDelClienteParaElEventoIndicado() {
        Orden orden = ordenPendiente();
        when(ordenRepository.findByClienteIdAndEventoIdOrderByFechaCreacionDesc(7L, "evt-1"))
                .thenReturn(List.of(orden));

        List<OrdenResponseDTO> resultado = service.listar(7L, "evt-1");

        assertEquals(1, resultado.size());
        assertEquals("evt-1", resultado.get(0).getEventoId());
    }

    @Test
    void listaTodasLasOrdenesDelClienteCuandoNoSeIndicaEvento() {
        Orden orden = ordenPendiente();
        when(ordenRepository.findByClienteIdOrderByFechaCreacionDesc(7L)).thenReturn(List.of(orden));

        List<OrdenResponseDTO> resultado = service.listar(7L, null);

        assertEquals(1, resultado.size());
    }
}
