package org.example.orden.controller;

import lombok.AllArgsConstructor;
import org.example.orden.dto.response.ReporteEventoDTO;
import org.example.orden.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/ordenes/reportes")
public class ReporteEventoController {
    private final ReporteService reporteService;

    @GetMapping("/{idEvento}")
    public ResponseEntity<ReporteEventoDTO> getReporteEventos(@PathVariable String idEvento) {
        ReporteEventoDTO reporte = reporteService.generarReporte(idEvento);
        return ResponseEntity.ok(reporte);
    }

}
