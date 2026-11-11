package org.example.reserva.service;

import org.example.reserva.dto.request.EventoExternoDTO;
import org.example.reserva.dto.request.LugarExternoDTO;
import org.example.reserva.dto.request.ReservaRequestDTO;
import org.example.reserva.dto.request.SectorExternoDTO;
import org.example.reserva.dto.response.ReservaResponseDTO;
import org.example.reserva.entity.Reserva;
import org.example.reserva.feignClients.EventosClient;
import org.example.reserva.mapper.ReservaMapper;
import org.example.reserva.repository.ReservaRepository;
import org.example.reserva.utils.EstadoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock private ReservaRepository repository;
    @Mock private EventosClient eventosClient;

    private ReservaService service;

    @BeforeEach
    void setUp() {
        service = new ReservaService(new ReservaMapper(), repository, eventosClient);
        lenient().when(repository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(eventosClient.obtenerEvento("evt-1")).thenReturn(new EventoExternoDTO("evt-1", "lugar-1", "PUBLICADO", 0f));
        lenient().when(eventosClient.obtenerLugar("lugar-1")).thenReturn(new LugarExternoDTO("lugar-1", List.of(
                new SectorExternoDTO("Platea", "NUMERADO", 10, 20, null),      // como lo devuelve Evento: sin capacidad
                new SectorExternoDTO("Campo", "NO_NUMERADO", null, null, 100))));
    }

    private ReservaRequestDTO numerada(List<Long> asientos) {
        return new ReservaRequestDTO(7L, "evt-1", "Platea", asientos, null);
    }

    private ReservaRequestDTO zona(int cantidad) {
        return new ReservaRequestDTO(7L, "evt-1", "Campo", null, cantidad);
    }

    // ---------- Sector numerado ----------

    @Test
    void reservaAsientosNumeradosYLosGuarda() {
        when(repository.contarAsientosOcupados(eq("evt-1"), anyList(), eq(List.of(45L, 46L)))).thenReturn(0L);

        ReservaResponseDTO dto = service.crearReserva(numerada(List.of(45L, 46L)));

        assertEquals(EstadoReserva.PENDIENTE, dto.getEstado());
        ArgumentCaptor<Reserva> guardada = ArgumentCaptor.forClass(Reserva.class);
        verify(repository).save(guardada.capture());
        assertEquals(List.of(45L, 46L), guardada.getValue().getAsientos());
        assertEquals(2, guardada.getValue().getCantidad());
        assertNotNull(guardada.getValue().getFechaVencimiento());
    }

    @Test
    void noReservaUnAsientoOcupado() {
        when(repository.contarAsientosOcupados(eq("evt-1"),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)), anyList())).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.crearReserva(numerada(List.of(45L))));
        verify(repository, never()).save(any());
    }

    @Test
    void unAsientoFueraDelSectorEsInvalido() {
        // Platea: 10 filas x 20 asientos = 200
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(numerada(List.of(201L))));
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(numerada(List.of(0L))));
        verify(repository, never()).save(any());
    }

    // ---------- Sector no numerado (zona) ----------

    @Test
    void reservaLugaresEnUnaZonaConCapacidad() {
        when(repository.contarEntradasVendidasPorSector(eq("evt-1"), eq("Campo"), anyList())).thenReturn(98L);

        ReservaResponseDTO dto = service.crearReserva(zona(2));

        assertEquals(EstadoReserva.PENDIENTE, dto.getEstado());
        verify(repository, never()).contarAsientosOcupados(any(), any(), any());
    }

    @Test
    void noSeVendeMasQueLaCapacidadDeLaZona() {
        // quedan 2 lugares en el Campo y se piden 3
        when(repository.contarEntradasVendidasPorSector(eq("evt-1"), eq("Campo"), anyList())).thenReturn(98L);

        assertThrows(IllegalStateException.class, () -> service.crearReserva(zona(3)));
        verify(repository, never()).save(any());
    }

    // ---------- Validaciones generales ----------

    @Test
    void hayQuePedirAlMenosUnaEntrada() {
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(zona(0)));
    }

    @Test
    void unSectorInexistenteEsInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearReserva(new ReservaRequestDTO(7L, "evt-1", "Palco", List.of(1L), null)));
    }

    // ---------- Estados ----------

    @Test
    void lasReservasVencidasPasanAVencida() {
        Reserva vieja = new Reserva(7L, "evt-1", "Platea");
        when(repository.findByEstadoAndFechaVencimientoBefore(eq(EstadoReserva.PENDIENTE), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>(List.of(vieja)));

        service.liberarReservasVencidas();

        assertEquals(EstadoReserva.VENCIDA, vieja.getEstado());
    }

    @Test
    void actualizarEstadoConfirmaUnaReserva() {
        Reserva r = new Reserva(7L, "evt-1", "Platea");
        when(repository.findById(1L)).thenReturn(Optional.of(r));

        service.actualizarEstado(1L, "confirmada");

        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }
}
