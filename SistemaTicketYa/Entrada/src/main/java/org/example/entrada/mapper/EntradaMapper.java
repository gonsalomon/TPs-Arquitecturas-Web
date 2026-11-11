package org.example.entrada.mapper;

import org.example.entrada.dto.EntradaDTO;
import org.example.entrada.dto.response.EntradaEmitidaDTO;
import org.example.entrada.entity.Entrada;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntradaMapper {

    public EntradaDTO toDto(Entrada e) {
        if (e == null) return null;
        return new EntradaDTO(
                e.getId(),
                e.getEventoId(),
                e.getSectorId(),
                e.getClienteId(),
                e.getOrdenId(),
                e.getFila(),
                e.getAsiento(),
                e.getPrecio(),
                e.getFechaEvento(),
                e.getEstado(),
                e.getCodigoQr()
        );
    }

    public List<EntradaDTO> toDtos(List<Entrada> entradas) {
        return entradas.stream().map(this::toDto).toList();
    }

        public EntradaEmitidaDTO toEmitidaDto(Entrada e) {
        if (e == null) return null;
        return new EntradaEmitidaDTO(e.getId(), e.getCodigoQr(), e.getSectorId(), e.getFila(), e.getAsiento());
    }

    public List<EntradaEmitidaDTO> toEmitidaDtos(List<Entrada> entradas) {
        return entradas.stream().map(this::toEmitidaDto).toList();
    }
}