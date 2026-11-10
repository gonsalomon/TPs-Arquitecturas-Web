package org.example.entrada;

import org.example.entrada.client.PagoFeignClient;
import org.example.entrada.client.ReservaFeignClient;
import org.example.entrada.client.UsuarioFeignClient;
import org.example.entrada.dto.CambioEstadoRequest;
import org.example.entrada.dto.EmitirEntradasRequest;
import org.example.entrada.dto.EntradaDTO;
import org.example.entrada.dto.ItemEntradaRequest;
import org.example.entrada.entity.Entrada;
import org.example.entrada.exception.DevolucionFueraDePlazoException;
import org.example.entrada.exception.LimiteEntradasExcedidoException;
import org.example.entrada.mapper.EntradaMapper;
import org.example.entrada.repository.EntradaRepository;
import org.example.entrada.service.EntradaService;
import org.example.entrada.utils.EstadoEntrada;
import org.example.entrada.utils.MotivoCambio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure unit tests: the repository is mocked, no database needed
class EntradaServiceTest {

    private EntradaRepository repo;
    private UsuarioFeignClient usuarioFeignClient;
    private PagoFeignClient pagoFeignClient;
    private ReservaFeignClient reservaFeignClient;
    private EntradaService service;

    @BeforeEach
    void setUp() {
        repo = mock(EntradaRepository.class);
        usuarioFeignClient = mock(UsuarioFeignClient.class);
        pagoFeignClient = mock(PagoFeignClient.class);
        reservaFeignClient = mock(ReservaFeignClient.class);
        service = new EntradaService(repo, new EntradaMapper(), 6,usuarioFeignClient, pagoFeignClient, reservaFeignClient);
    }

    private Entrada entradaValida(LocalDateTime fechaEvento) {
        Entrada e = new Entrada();
        e.setEventoId("ev1");
        e.setSectorId("S1");
        e.setClienteId(1L);
        e.setOrdenId(10L);
        e.setFila(1);
        e.setAsiento(1);
        e.setPrecio(new BigDecimal("100"));
        e.setFechaEvento(fechaEvento);
        e.setClaveIdempotencia("10-0");
        e.cambiarEstado(EstadoEntrada.VALIDA);   // computes the active seat key
        return e;
    }

    private EmitirEntradasRequest pedido() {
        return new EmitirEntradasRequest(10L, "ev1", 1L, LocalDateTime.now().plusDays(30),
                List.of(new ItemEntradaRequest("S1", 1, 1, new BigDecimal("100"))));
    }

    @Test
    void devolucionDentroDePlazoLiberaElAsiento() {
        Entrada e = entradaValida(LocalDateTime.now().plusDays(5));
        when(repo.findById(1L)).thenReturn(Optional.of(e));
        assertNotNull(e.getClaveAsientoActivo());

        EntradaDTO dto = service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION));

        assertEquals(EstadoEntrada.REINTEGRO, dto.estado());
        assertNull(e.getClaveAsientoActivo());   // the seat can be sold again
    }

    @Test
    void devolucionAMenosDe72HorasSeRechaza() {
        Entrada e = entradaValida(LocalDateTime.now().plusHours(48));
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(DevolucionFueraDePlazoException.class,
                () -> service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION)));
        assertEquals(EstadoEntrada.VALIDA, e.getEstado());
    }

    @Test
    void cancelacionDeEventoIgnoraElPlazoDe72Horas() {
        Entrada e = entradaValida(LocalDateTime.now().plusHours(1));
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        EntradaDTO dto = service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.EVENTO_CANCELADO));

        assertEquals(EstadoEntrada.REINTEGRO, dto.estado());
    }

    @Test
    void reintegroSinMotivoEsInvalido() {
        Entrada e = entradaValida(LocalDateTime.now().plusDays(5));
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, null)));
    }

    @Test
    void transicionNoPermitidaSeRechaza() {
        Entrada e = entradaValida(LocalDateTime.now().plusDays(5));
        e.cambiarEstado(EstadoEntrada.REINTEGRO);
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.USADA, null)));
    }

    @Test
    void repetirElMismoCambioEsIdempotente() {
        Entrada e = entradaValida(LocalDateTime.now().plusDays(5));
        e.cambiarEstado(EstadoEntrada.REINTEGRO);
        when(repo.findById(1L)).thenReturn(Optional.of(e));

        EntradaDTO dto = service.cambiarEstado(1L, new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION));

        assertEquals(EstadoEntrada.REINTEGRO, dto.estado());
    }

    @Test
    void cancelarEventoPasaTodasLasValidasAReintegro() {
        Entrada a = entradaValida(LocalDateTime.now().plusDays(10));
        Entrada b = entradaValida(LocalDateTime.now().plusDays(10));
        when(repo.findByEventoIdAndEstado("ev1", EstadoEntrada.VALIDA)).thenReturn(List.of(a, b));

        List<EntradaDTO> afectadas = service.reintegroPorEvento("ev1");

        assertEquals(2, afectadas.size());
        assertEquals(EstadoEntrada.REINTEGRO, a.getEstado());
        assertEquals(EstadoEntrada.REINTEGRO, b.getEstado());
    }

    @Test
    void emitirSuperandoElLimiteDelClienteSeRechaza() {
        when(repo.countByClienteIdAndEventoIdAndEstadoIn(eq(1L), eq("ev1"), anyCollection())).thenReturn(6L);

        assertThrows(LimiteEntradasExcedidoException.class, () -> service.emitir(pedido()));
        verify(repo, never()).saveAllAndFlush(anyList());
    }

    @Test
    void reintentoConLaMismaOrdenNoCreaEntradasNuevas() {
        Entrada existente = entradaValida(LocalDateTime.now().plusDays(30));
        when(repo.findByOrdenId(10L)).thenReturn(List.of(existente));

        List<EntradaDTO> r = service.emitir(pedido());

        assertEquals(1, r.size());
        verify(repo, never()).saveAllAndFlush(anyList());
    }
}