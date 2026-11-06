package org.example.orden.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.example.orden.entity.Orden;
import org.example.orden.feignClients.ReservaClient;
import org.example.orden.repository.OrdenRepository;
import org.example.orden.utils.EstadoOrden;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sin este job, un cliente que abandona una orden PENDIENTE_PAGO deja los asientos
 * bloqueados en Orden para siempre, porque la clave única de ItemOrden nunca se libera sola.
 */
@Component
@RequiredArgsConstructor
public class OrdenVencimientoJob {

    private final OrdenRepository ordenRepository;
    private final ReservaClient reservaClient;

    @Scheduled(fixedDelayString = "${orden.vencimiento.intervalo-ms}")
    @Transactional
    public void vencerOrdenesPendientes() {
        List<Orden> vencidas = ordenRepository.buscarPendientesVencidas(LocalDateTime.now());
        for (Orden orden : vencidas) {
            vencerOrden(orden);
        }
    }

    void vencerOrden(Orden orden) {
        orden.cambiarEstado(EstadoOrden.VENCIDA);
        orden.liberarClaves();
        ordenRepository.save(orden);
        liberarReservaSilenciosamente(orden.getIdReserva());
    }

    private void liberarReservaSilenciosamente(Long idReserva) {
        if (idReserva == null) {
            return;
        }
        try {
            reservaClient.actualizarEstado(idReserva, "LIBERADA");
        } catch (FeignException ignored) {
            // liberar una reserva ya liberada o con el servicio caído no debe interrumpir al job
        }
    }
}
