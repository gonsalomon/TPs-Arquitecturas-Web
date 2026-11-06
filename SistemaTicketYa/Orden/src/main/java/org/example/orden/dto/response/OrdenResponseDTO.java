package org.example.orden.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenResponseDTO {
    private Long id;
    private Long clienteId;
    private String eventoId;
    private String sector;
    private String estado;
    private BigDecimal total;
    private String medioPago;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaVencimiento;
    private List<ItemOrdenResponseDTO> items;
    private List<EntradaEmitidaDTO> entradas;
}
