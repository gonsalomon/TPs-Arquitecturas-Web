package org.example.entrada.service;

import org.example.entrada.dto.CambioEstadoRequest;
import org.example.entrada.dto.EmitirEntradasRequest;
import org.example.entrada.dto.EntradaDTO;
import org.example.entrada.dto.ItemEntradaRequest;
import org.example.entrada.exception.AsientoNoDisponibleException;
import org.example.entrada.repository.EntradaRepository;
import org.example.entrada.utils.EstadoEntrada;
import org.example.entrada.utils.MotivoCambio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests against the REAL database (the MySQL container must be running: docker compose up -d).
 * Each test uses a random eventoId, so runs don't interfere with each other.
 * Nothing is rolled back on purpose: concurrency needs real commits.
 */
@SpringBootTest
class EntradaConcurrenciaTest {

    @Autowired
    EntradaService service;

    @Autowired
    EntradaRepository repo;

    private static String nuevoEvento() {
        return "test-" + UUID.randomUUID();
    }

    private static EmitirEntradasRequest pedido(long ordenId, String eventoId, long clienteId) {
        return new EmitirEntradasRequest(ordenId, eventoId, clienteId, LocalDateTime.now().plusDays(30),
                List.of(new ItemEntradaRequest("S1", 3, 10, new BigDecimal("5000"))));
    }

    @Test
    void veinteCompradoresDelMismoAsientoSoloUnoGana() throws Exception {
        String eventoId = nuevoEvento();
        int hilos = 20;
        long base = System.nanoTime();
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        for (int i = 0; i < hilos; i++) {
            long ordenId = base + i;
            long clienteId = 1000 + i;               // different clients, so the per-client limit doesn't interfere
            resultados.add(pool.submit(() -> {
                largada.await();                     // all threads start together
                try {
                    service.emitir(pedido(ordenId, eventoId, clienteId));
                    return true;
                } catch (Exception e) {
                    return false;                    // normally AsientoNoDisponibleException
                }
            }));
        }
        largada.countDown();

        int exitos = 0;
        for (Future<Boolean> f : resultados) {
            if (f.get()) exitos++;
        }
        pool.shutdown();

        assertEquals(1, exitos, "exactly one purchase must succeed");
        assertEquals(1, repo.findByEventoId(eventoId).size(), "exactly one ticket must exist for the seat");
    }

    @Test
    void reintentoDeLaMismaOrdenDevuelveLasMismasEntradas() {
        String eventoId = nuevoEvento();
        long ordenId = System.nanoTime();

        List<EntradaDTO> primera = service.emitir(pedido(ordenId, eventoId, 7L));
        List<EntradaDTO> segunda = service.emitir(pedido(ordenId, eventoId, 7L));

        assertEquals(primera.get(0).id(), segunda.get(0).id());
        assertEquals(1, repo.findByEventoId(eventoId).size());
    }

    @Test
    void otraOrdenNoPuedeVenderUnAsientoYaVendido() {
        String eventoId = nuevoEvento();
        long orden = System.nanoTime();

        service.emitir(pedido(orden, eventoId, 1L));

        assertThrows(AsientoNoDisponibleException.class, () -> service.emitir(pedido(orden + 1, eventoId, 2L)));
    }

    @Test
    void asientoLiberadoPorReintegroPuedeVenderseDeNuevo() {
        String eventoId = nuevoEvento();
        long orden = System.nanoTime();

        List<EntradaDTO> primera = service.emitir(pedido(orden, eventoId, 1L));
        service.cambiarEstado(primera.get(0).id(),
                new CambioEstadoRequest(EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION));

        List<EntradaDTO> segunda = service.emitir(pedido(orden + 1, eventoId, 2L));

        assertEquals(1, segunda.size());
        assertEquals(2, repo.findByEventoId(eventoId).size());   // the refunded one and the new one
    }
}