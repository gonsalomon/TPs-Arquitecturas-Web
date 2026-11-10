//package org.example.reserva.service;
//
//import org.example.reserva.dto.request.*;
//import org.example.reserva.dto.response.ReservaResponseDTO;
//import org.example.reserva.entity.Reserva;
//import org.example.reserva.feignClients.EventosClient;
//import org.example.reserva.mapper.ReservaMapper;
//import org.example.reserva.repository.ReservaRepository;
//import org.example.reserva.utils.EstadoReserva;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ReservaServiceTest {
//
//    @Mock private ReservaRepository repository;
//    @Mock private EventosClient eventosClient;
//
//    private ReservaService service;
//
//    @BeforeEach
//    void setUp() {
//        service = new ReservaService(new ReservaMapper(), repository, eventosClient);
//        lenient().when(repository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
//        lenient().when(eventosClient.obtenerEvento("evt-1")).thenReturn(new EventoExternoDTO("evt-1", "lugar-1", "PUBLICADO", 0f));
//        lenient().when(eventosClient.obtenerLugar("lugar-1")).thenReturn(new LugarExternoDTO("lugar-1", List.of(
//                new SectorExternoDTO("Platea", "NUMERADO", 10, 20, null),      // como lo devuelve Evento: sin capacidad
//                new SectorExternoDTO("Campo", "NO_NUMERADO", null, null, 5000))));
//    }
//
////    private ReservaRequestDTO pedido(String sector, List<Long> asientos) {
////        return new ReservaRequestDTO(7L, "evt-1", sector, asientos);
////    }
////
////    @Test
////    void reservaAsientosNumeradosYLosGuarda() {
////        when(repository.contarAsientosOcupados(eq("evt-1"), anyList(), eq(List.of(45L, 46L)))).thenReturn(0L);
////
////        ReservaResponseDTO dto = service.crearReserva(pedido("Platea", List.of(45L, 46L)));
////
////        assertEquals(EstadoReserva.PENDIENTE, dto.getEstado());
////        ArgumentCaptor<Reserva> guardada = ArgumentCaptor.forClass(Reserva.class);
////        verify(repository).save(guardada.capture());
////        assertEquals(List.of(45L, 46L), guardada.getValue().getAsientos());
////        assertNotNull(guardada.getValue().getFechaVencimiento());
////    }
//
//    @Test
//    void noReservaUnAsientoOcupado() {
//        when(repository.contarAsientosOcupados(eq("evt-1"),
//                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)), anyList())).thenReturn(1L);
//
//        assertThrows(IllegalStateException.class, () -> service.crearReserva(pedido("Platea", List.of(45L))));
//        verify(repository, never()).save(any());
//    }
//
//    @Test
//    void unAsientoFueraDelSectorEsInvalido() {
//        // Platea tiene 10 filas x 20 asientos = 200
//        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(pedido("Platea", List.of(201L))));
//        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(pedido("Platea", List.of(0L))));
//    }
//
//    @Test
//    void unSectorInexistenteEsInvalido() {
//        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(pedido("Palco", List.of(1L))));
//    }
//
//    @Test
//    void unaZonaSinAsientosSeReservaSinConsultarOcupados() {
//        ReservaResponseDTO dto = service.crearReserva(pedido("Campo", null));
//
//        assertEquals(EstadoReserva.PENDIENTE, dto.getEstado());
//        verify(repository, never()).contarAsientosOcupados(any(), any(), any());
//    }
//
//    @Test
//    void lasReservasVencidasPasanAVencida() {
//        Reserva vieja = new Reserva(7L, "evt-1", "Platea");
//        when(repository.findByEstadoAndFechaVencimientoBefore(eq(EstadoReserva.PENDIENTE), any(LocalDateTime.class)))
//                .thenReturn(new ArrayList<>(List.of(vieja)));
//
//        service.liberarReservasVencidas();
//
//        assertEquals(EstadoReserva.VENCIDA, vieja.getEstado());
//        verify(repository).saveAll(anyList());
//    }
//
//    @Test
//    void actualizarEstadoConfirmaUnaReserva() {
//        Reserva r = new Reserva(7L, "evt-1", "Platea");
//        when(repository.findById(1L)).thenReturn(Optional.of(r));
//
//        service.actualizarEstado(1L, "confirmada");
//
//        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
//    }
//}
