package org.example.orden.service;

import org.example.orden.entity.ItemOrden;
import org.example.orden.entity.Orden;
import org.example.orden.feignClients.ReservaClient;
import org.example.orden.repository.OrdenRepository;
import org.example.orden.utils.EstadoOrden;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenVencimientoJobTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private ReservaClient reservaClient;

    @Test
    void venceUnaOrdenPendienteYLiberaSusClavesYLaReserva() {
        OrdenVencimientoJob job = new OrdenVencimientoJob(ordenRepository, reservaClient);
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));

        Orden orden = new Orden(7L, "evt-1", "Platea", LocalDateTime.now().plusDays(1));
        orden.agregarItem(new ItemOrden(3, 5, 45, new BigDecimal("15000")));
        orden.setIdReserva(31L);
        orden.setFechaVencimiento(LocalDateTime.now().minusMinutes(1));

        job.vencerOrden(orden);

        assertEquals(EstadoOrden.VENCIDA, orden.getEstado());
        assertNull(orden.getItems().get(0).getClaveAsientoActivo());
        verify(reservaClient).actualizarEstado(31L, "LIBERADA");
    }
}
