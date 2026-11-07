package org.example.evento.mapper;

import org.example.evento.dto.request.LugarRequestDTO;
import org.example.evento.dto.request.SectorRequestDTO;
import org.example.evento.dto.response.LugarResponseDTO;
import org.example.evento.entity.Lugar;
import org.example.evento.entity.Sector;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LugarMapper {

    public LugarResponseDTO convertToDTO(Lugar lugar) {
        return new LugarResponseDTO(
                lugar.getIdLugar(),
                lugar.getNombre(),
                lugar.getDireccion(),
                lugar.getCiudad(),
                lugar.getLatitud(),
                lugar.getLongitud(),
                lugar.getSectores()
        );
    }

    public Lugar convertToEntity(LugarRequestDTO dto) {
        return new Lugar(
                dto.getNombre(),
                dto.getDireccion(),
                dto.getCiudad(),
                dto.getLatitud(),
                dto.getLongitud(),
                convertSectores(dto.getSectores())
        );
    }

    public List<Sector> convertSectores(List<SectorRequestDTO> sectores) {
        return sectores.stream()
                .map(s -> new Sector(s.getNombre(), s.getTipo(), s.getFilas(), s.getAsientosPorFila(), s.getCapacidad()))
                .toList();
    }
}
