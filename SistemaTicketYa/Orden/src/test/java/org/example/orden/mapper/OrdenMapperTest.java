package org.example.orden.mapper;

import org.example.orden.dto.response.OrdenResponseDTO;
import org.example.orden.entity.ItemOrden;
import org.example.orden.entity.Orden;
import org.example.orden.utils.EstadoOrden;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrdenMapperTest {

    private final OrdenMapper mapper = new OrdenMapper();

    @Test
    void convierteOrdenADTOConSusItems() {
        Orden orden = new Orden(7L, "evt-1", "Platea", LocalDateTime.of(2026, 12, 10, 21, 0));
        orden.agregarItem(new ItemOrden(3, 5, 45, new BigDecimal("15000.00")));
        orden.setFechaVencimiento(LocalDateTime.of(2026, 11, 5, 18, 40));

        OrdenResponseDTO dto = mapper.convertToDTO(orden);

        assertEquals(7L, dto.getClienteId());
        assertEquals("evt-1", dto.getEventoId());
        assertEquals("Platea", dto.getSector());
        assertEquals(EstadoOrden.PENDIENTE_PAGO.name(), dto.getEstado());
        assertEquals(new BigDecimal("15000.00"), dto.getTotal());
        assertEquals(1, dto.getItems().size());
        assertEquals(3, dto.getItems().get(0).getFila());
        assertEquals(5, dto.getItems().get(0).getAsiento());
    }
}
