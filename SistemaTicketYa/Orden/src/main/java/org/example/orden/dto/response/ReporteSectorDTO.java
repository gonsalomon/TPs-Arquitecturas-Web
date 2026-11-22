package org.example.orden.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteSectorDTO {
    private String sector;
    private Integer capacidad;
    private Integer entradasVendidas;
    private Integer entradasDevueltas;
    private Double porcentajeOcupacion;
    private BigDecimal recaudacion;
}
