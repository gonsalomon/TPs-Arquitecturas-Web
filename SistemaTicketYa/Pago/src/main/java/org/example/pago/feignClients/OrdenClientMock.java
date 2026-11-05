package org.example.pago.feignClients;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class OrdenClientMock implements OrdenClient {

    @Override
    public void notificarResultadoPago(Long idReserva, String estado) {
        // Simulamos que la comunicación con el orquestador fue exitosa
        System.out.println("MOCK ORDEN: Se notificó a la Orden que el pago de la reserva "
                + idReserva + " resultó: " + estado);
    }
}