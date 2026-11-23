package org.example.orden.service;

import lombok.AllArgsConstructor;
import org.example.orden.dto.response.*;
import org.example.orden.entity.ItemOrden;
import org.example.orden.entity.Orden;
import org.example.orden.feignClients.EntradaClient;
import org.example.orden.feignClients.EventoClient;
import org.example.orden.repository.OrdenRepository;
import org.example.orden.utils.EstadoOrden;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ReporteService {
    private final OrdenRepository ordenRepository;
    private final EventoClient eventoClient;
    private final EntradaClient entradaClient;

    public ReporteEventoDTO generarReporte(String eventoId) {

        List<Orden> ordenes = ordenRepository.findByEventoId(eventoId);

        // Obtener los sectores desde Evento
        EventoExternoDTO evento = eventoClient.obtenerEvento(eventoId);
        LugarExternoDTO lugar = eventoClient.obtenerLugar(evento.getIdLugar());

        List<SectorExternoDTO> sectores = lugar.getSectores();

        // Agrupar órdenes por objeto SectorExternoDTO
        Map<SectorExternoDTO, List<Orden>> ordenesPorSector = new HashMap<>();
        int capacidadEvento = 0;
        for (SectorExternoDTO sector : sectores) {
            List<Orden> ordenesSector = ordenes.stream()  //guardo solo las que cumplen la condicion
                    .filter(o -> o.getSector().equalsIgnoreCase(sector.getNombre()))
                    .toList();
            ordenesPorSector.put(sector, ordenesSector);
            capacidadEvento += sector.getCapacidad();
        }

        List<ReporteSectorDTO> reporteSectores = new ArrayList<>();
        int totalEntradasVendidas = 0;
        int totalEntradasDevueltas = 0;
        BigDecimal totalRecaudacion = BigDecimal.ZERO;

        for (Map.Entry<SectorExternoDTO, List<Orden>> entry : ordenesPorSector.entrySet()) {
            SectorExternoDTO sector = entry.getKey();
            List<Orden> ordenesSector = entry.getValue();

            int entradasVendidas = 0;
            int entradasDevueltas = 0;
            BigDecimal recaudacion = BigDecimal.ZERO;

            for (Orden orden : ordenesSector) {
                if (orden.getEstado() == EstadoOrden.CONFIRMADA) {
                    // llamar al get de entradas por el order id y con estado valida, contarlas y sumarlas
                    List<EntradaDTO> entradas = entradaClient.getEntradas("VALIDA",orden.getIdOrden());
                    entradasVendidas += entradas.size();
                    totalEntradasVendidas += entradasVendidas;
                    BigDecimal precioUnitario = orden.getItems().stream()
                            .map(ItemOrden::getPrecioUnitario)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal importe = precioUnitario
                            .multiply(BigDecimal.valueOf(entradas.size()))
                            .divide(BigDecimal.valueOf(orden.getItems().size()));
                    recaudacion = recaudacion.add(importe);
                    totalRecaudacion= totalRecaudacion.add(importe);

                    // llamar al get de entradas por el order id y con estado reintegro, contarlas y sumarlas
                    List<EntradaDTO> entradasConReembolso = entradaClient.getEntradas("REINTEGRO",orden.getIdOrden());
                    entradasDevueltas += entradasConReembolso.size();
                    totalEntradasDevueltas += entradasDevueltas;
                }
            }
            int capacidad = sector.getCapacidad();
            double porcentajeOcupacion = capacidad == 0   //verifica que la capacidad no sea cero.
                    ? 0                         //si la capacidad es cero, asigna 0 a ocupacion.
                    : (double) entradasVendidas / capacidad * 100; // si la capacidad no es cero, ejecuta el cálculo.

            ReporteSectorDTO reporte = new ReporteSectorDTO(
                    sector.getNombre(),
                    capacidad,
                    entradasVendidas,
                    entradasDevueltas,
                    porcentajeOcupacion,
                    recaudacion
            );
            reporteSectores.add(reporte);
        }
        //aca calculo el porcentaje de ocupacion del evento
        Double totalPorcentajeOcupacion =  capacidadEvento == 0
                ? 0
                : (double) totalEntradasVendidas / capacidadEvento * 100;

        return new ReporteEventoDTO(
                eventoId,
                totalEntradasVendidas,
                totalEntradasDevueltas,
                totalPorcentajeOcupacion,
                totalRecaudacion,
                reporteSectores
        );
    }
}
