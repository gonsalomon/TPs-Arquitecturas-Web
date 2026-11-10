package org.example.entrada.repository;

import org.example.entrada.entity.Entrada;
import org.example.entrada.utils.EstadoEntrada;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EntradaRepositoryTest {

    @Autowired private EntradaRepository repo;

    private Entrada entrada(Long ordenId, String idempotencia, Integer fila, Integer asiento) {
        Entrada e = new Entrada();
        e.setEventoId("evt-1");
        e.setSectorId("Platea");
        e.setClienteId(7L);
        e.setOrdenId(ordenId);
        e.setFila(fila);
        e.setAsiento(asiento);
        e.setPrecio(new BigDecimal("1500.00"));
        e.setFechaEvento(LocalDateTime.now().plusDays(30));
        e.setClaveIdempotencia(idempotencia);
        return e;
    }

    @Test
    void dosOrdenesNoPuedenTenerElMismoAsiento() {
        repo.saveAndFlush(entrada(1L, "1-0", 1, 1));

        assertThrows(DataIntegrityViolationException.class,
                () -> repo.saveAndFlush(entrada(2L, "2-0", 1, 1)));
    }

    @Test
    void unAsientoReintegradoSePuedeVolverAVender() {
        Entrada vieja = repo.saveAndFlush(entrada(1L, "1-0", 1, 1));
        vieja.cambiarEstado(EstadoEntrada.REINTEGRO);
        repo.saveAndFlush(vieja);

        assertDoesNotThrow(() -> repo.saveAndFlush(entrada(2L, "2-0", 1, 1)));
    }

    @Test
    void laMismaClaveDeIdempotenciaNoSePuedeRepetir() {
        repo.saveAndFlush(entrada(1L, "1-0", 1, 1));

        assertThrows(DataIntegrityViolationException.class,
                () -> repo.saveAndFlush(entrada(1L, "1-0", 1, 2)));
    }

    @Test
    void lasZonasSinAsientoNoChocanEntreSi() {
        repo.saveAndFlush(entrada(1L, "1-0", null, null));

        assertDoesNotThrow(() -> repo.saveAndFlush(entrada(2L, "2-0", null, null)));
    }
}