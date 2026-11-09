package org.example.pago.mapper;

import org.example.pago.dto.request.PagoRequestDTO;
import org.example.pago.dto.response.PagoResponseDTO;
import org.example.pago.entity.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public PagoResponseDTO convertToDTO(Pago pago){
        return new PagoResponseDTO(
                pago.getIdPago(),
                pago.getIdCliente(),
                pago.getIdReserva(),
                pago.getEstado(),
                pago.getMetodoPago(),
                pago.getMonto(),
                pago.getFecha_emision()
        );
    }

    public Pago convertToEntity(PagoRequestDTO pagoRequestDTO){

        return new Pago(
                pagoRequestDTO.getIdReserva(),
                pagoRequestDTO.getIdCliente(),
                pagoRequestDTO.getMetodoPago()
        );
    }
}
