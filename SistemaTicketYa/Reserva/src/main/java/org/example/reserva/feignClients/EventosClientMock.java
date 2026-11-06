package org.example.reserva.feignClients;

import org.example.reserva.dto.request.EventoExternoDTO;
import org.example.reserva.dto.request.LugarExternoDTO;
import org.example.reserva.dto.request.SectorExternoDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.util.List;

@Primary // Le dice a Spring que inyecte esta clase en lugar del FeignClient real
@Component
public class EventosClientMock implements EventosClient {

    @Override
    public EventoExternoDTO obtenerEvento(String id) {
        EventoExternoDTO evento = new EventoExternoDTO();
        evento.setId(id);
        evento.setIdLugar("1L");
        return evento;
    }

    @Override
    public LugarExternoDTO obtenerLugar(String id) {
        SectorExternoDTO sector = new SectorExternoDTO();
        sector.setNombre("Platea VIP");
        sector.setPrecio(5000.0f);
        sector.setAsientosPermitidos(List.of(100L, 101L, 102L, 103L)); // Asientos hardcodeados

        LugarExternoDTO lugar = new LugarExternoDTO();
        lugar.setId(id);
        lugar.setSectores(List.of(sector));

        return lugar;
    }
}
