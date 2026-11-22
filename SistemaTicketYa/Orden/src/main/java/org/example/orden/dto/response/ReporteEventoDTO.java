package org.example.orden.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteEventoDTO {
    private String eventoId;
    private Integer entradasVendidas;
    private Integer entradasDevueltas;
    private Double porcentajeOcupacion;
    private BigDecimal recaudacionTotal;

    private List<ReporteSectorDTO> reporteSectores;
}