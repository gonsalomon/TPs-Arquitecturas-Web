package org.example.orden.repository;

import org.example.orden.entity.ItemOrden;
import org.example.orden.entity.Orden;
import org.example.orden.utils.EstadoOrden;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrdenRepositoryTest {

    @Autowired
    private OrdenRepository ordenRepository;

    private Orden nuevaOrden(Long clienteId, String eventoId, String sector, EstadoOrden estado) {
        Orden orden = new Orden(clienteId, eventoId, sector, LocalDateTime.now().plusDays(1));
        orden.setEstado(estado);
        orden.setFechaVencimiento(LocalDateTime.now().plusMinutes(10));
        return orden;
    }

    @Test
    void laSumaDeEntradasActivasIgnoraRechazadasYVencidas() {
        String eventoId = "evt-suma-" + System.nanoTime();
        Long clienteId = 1001L;

        Orden activa = nuevaOrden(clienteId, eventoId, "Platea", EstadoOrden.CONFIRMADA);
        activa.agregarItem(new ItemOrden(1, 1, 1, BigDecimal.TEN));
        activa.agregarItem(new ItemOrden(1, 2, 2, BigDecimal.TEN));
        ordenRepository.saveAndFlush(activa);

        Orden rechazada = nuevaOrden(clienteId, eventoId, "Platea", EstadoOrden.RECHAZADA);
        rechazada.agregarItem(new ItemOrden(2, 1, 21, BigDecimal.TEN));
        ordenRepository.saveAndFlush(rechazada);

        Orden vencida = nuevaOrden(clienteId, eventoId, "Platea", EstadoOrden.VENCIDA);
        vencida.agregarItem(new ItemOrden(3, 1, 41, BigDecimal.TEN));
        ordenRepository.saveAndFlush(vencida);

        long total = ordenRepository.contarEntradasActivasPorClienteYEvento(clienteId, eventoId);

        assertEquals(2, total);
    }

    @Test
    void dosItemsActivosConLaMismaClaveLanzanViolacionDeIntegridad() {
        String eventoId = "evt-clave-" + System.nanoTime();

        Orden primera = nuevaOrden(2001L, eventoId, "Campo", EstadoOrden.PENDIENTE_PAGO);
        primera.agregarItem(new ItemOrden(1, 5, 5, BigDecimal.TEN));
        ordenRepository.saveAndFlush(primera);

        Orden segunda = nuevaOrden(2002L, eventoId, "Campo", EstadoOrden.PENDIENTE_PAGO);
        segunda.agregarItem(new ItemOrden(1, 5, 5, BigDecimal.TEN));

        assertThrows(DataIntegrityViolationException.class, () -> ordenRepository.saveAndFlush(segunda));
    }

    @Test
    void dosClavesNulasNoLanzanViolacionDeIntegridad() {
        String eventoId = "evt-null-" + System.nanoTime();

        Orden primera = nuevaOrden(3001L, eventoId, "Campo", EstadoOrden.RECHAZADA);
        primera.agregarItem(new ItemOrden(1, 7, 7, BigDecimal.TEN));
        ordenRepository.saveAndFlush(primera);

        Orden segunda = nuevaOrden(3002L, eventoId, "Campo", EstadoOrden.RECHAZADA);
        segunda.agregarItem(new ItemOrden(1, 7, 7, BigDecimal.TEN));
        ordenRepository.saveAndFlush(segunda);
    }
}
