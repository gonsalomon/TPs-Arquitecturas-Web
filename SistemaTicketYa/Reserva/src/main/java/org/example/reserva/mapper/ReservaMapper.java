package org.example.reserva.mapper;

import org.example.reserva.dto.request.ReservaRequestDTO;
import org.example.reserva.dto.response.ReservaResponseDTO;
import org.example.reserva.entity.Reserva;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservaMapper {


    public Reserva convertToEntity(ReservaRequestDTO requestDTO) {
        return new Reserva(
                requestDTO.getIdCliente(),
                requestDTO.getIdEvento(),
                requestDTO.getSector(),
                requestDTO.getAsientos()
        );
    }

    public ReservaResponseDTO convertToDTO(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getIdReserva(),
                reserva.getIdCliente(),
                reserva.getIdEvento(),
                reserva.getSector(),
                reserva.getAsientos(),
                reserva.getEstado(),
                reserva.getFecha_inicio(),
                reserva.getFecha_vencimiento()
        );
    }

}
