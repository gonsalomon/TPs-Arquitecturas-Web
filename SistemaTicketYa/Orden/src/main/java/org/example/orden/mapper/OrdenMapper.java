package org.example.orden.mapper;

import org.example.orden.dto.response.ItemOrdenResponseDTO;
import org.example.orden.dto.response.OrdenResponseDTO;
import org.example.orden.entity.ItemOrden;
import org.example.orden.entity.Orden;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrdenMapper {

    public OrdenResponseDTO convertToDTO(Orden entity) {
        OrdenResponseDTO dto = new OrdenResponseDTO();
        dto.setId(entity.getIdOrden());
        dto.setClienteId(entity.getClienteId());
        dto.setEventoId(entity.getEventoId());
        dto.setSector(entity.getSector());
        dto.setEstado(entity.getEstado().name());
        dto.setTotal(entity.getTotal());
        dto.setMedioPago(entity.getMedioPago() != null ? entity.getMedioPago().name() : null);
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaVencimiento(entity.getFechaVencimiento());
        dto.setItems(convertItems(entity.getItems()));
        return dto;
    }

    private List<ItemOrdenResponseDTO> convertItems(List<ItemOrden> items) {
        return items.stream()
                .map(item -> new ItemOrdenResponseDTO(item.getFila(), item.getAsiento(), item.getPrecioUnitario()))
                .collect(Collectors.toList());
    }
}
