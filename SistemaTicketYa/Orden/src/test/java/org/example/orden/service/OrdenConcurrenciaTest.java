package org.example.orden.service;

import org.example.orden.dto.request.AsientoRequestDTO;
import org.example.orden.dto.request.CrearOrdenRequestDTO;
import org.example.orden.dto.response.EventoExternoDTO;
import org.example.orden.dto.response.LugarExternoDTO;
import org.example.orden.dto.response.PrecioSectorExternoDTO;
import org.example.orden.dto.response.ReservaExternaDTO;
import org.example.orden.dto.response.SectorExternoDTO;
import org.example.orden.entity.Orden;
import org.example.orden.exception.AsientoNoDisponibleException;
import org.example.orden.feignClients.EventoClient;
import org.example.orden.feignClients.ReservaClient;
import org.example.orden.repository.OrdenRepository;
import org.example.orden.utils.EstadoOrden;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Prueba la restricción única de clave_asiento_activo sobre el MySQL real (no H2):
 * la no doble venta depende de cómo el motor aplica la restricción y trata los NULL.
 */
@SpringBootTest
class OrdenConcurrenciaTest {

    @Autowired
    private OrdenService service;

    @Autowired
    private OrdenRepository ordenRepository;

    @MockBean
    private EventoClient eventoClient;

    @MockBean
    private ReservaClient reservaClient;

    @Test
    @Transactional
    void soloUnaOrdenQuedaActivaParaElMismoAsientoYLuegoSePuedeVolverAComprar() throws InterruptedException {
        String eventoId = "evt-concurrencia-" + System.nanoTime();
        configurarMocks(eventoId);

        int hilos = 50;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        CountDownLatch terminaron = new CountDownLatch(hilos);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger conflictos = new AtomicInteger();

        for (int i = 0; i < hilos; i++) {
            long clienteId = i + 1;
            executor.submit(() -> {
                try {
                    salida.await();
                    service.crear(clienteId, pedidoDelAsiento1x1(eventoId));
                    exitos.incrementAndGet();
                } catch (AsientoNoDisponibleException e) {
                    conflictos.incrementAndGet();
                } catch (InterruptedException ignored) {
                } finally {
                    terminaron.countDown();
                }
            });
        }

        salida.countDown();
        terminaron.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, exitos.get());
        assertEquals(hilos - 1, conflictos.get());

        Orden activa = ordenRepository.findAll().stream()
                .filter(o -> eventoId.equals(o.getEventoId()) && o.getEstado() == EstadoOrden.PENDIENTE_PAGO)
                .findFirst()
                .orElseThrow();
        activa.cambiarEstado(EstadoOrden.RECHAZADA);
        activa.liberarClaves();
        ordenRepository.save(activa);

        assertDoesNotThrow(() -> service.crear(999L, pedidoDelAsiento1x1(eventoId)));
    }

    private void configurarMocks(String eventoId) {
        EventoExternoDTO evento = new EventoExternoDTO();
        evento.setIdEvento(eventoId);
        evento.setEstado("PUBLICADO");
        evento.setIdLugar("lugar-1");
        evento.setFechaHora(LocalDateTime.now().plusDays(1));
        evento.setPrecios(List.of(new PrecioSectorExternoDTO("Campo", 10000.0)));
        when(eventoClient.obtenerEvento(eventoId)).thenReturn(evento);

        LugarExternoDTO lugar = new LugarExternoDTO(List.of(new SectorExternoDTO("Campo", "NUMERADO", 10, 20, 200)));
        when(eventoClient.obtenerLugar("lugar-1")).thenReturn(lugar);

        when(reservaClient.crearReserva(any())).thenReturn(new ReservaExternaDTO(
                1L, 1L, eventoId, "Campo", List.of(1L), "PENDIENTE",
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(10)));
    }

    private CrearOrdenRequestDTO pedidoDelAsiento1x1(String eventoId) {
        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO();
        request.setEventoId(eventoId);
        request.setSector("Campo");
        request.setAsientos(List.of(new AsientoRequestDTO(1, 1)));
        return request;
    }
}
