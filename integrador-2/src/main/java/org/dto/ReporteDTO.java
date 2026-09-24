package org.dto;

import java.util.List;

/**
 * Punto 3: reporte de carreras con inscriptos y egresados por año.
 * "carrera" es el nombre de la carrera; "detallePorAnio" es la lista de
 * años con sus contadores. Va como record anidado (DetalleAnio) y no como
 * variables sueltas porque cada carrera tiene varios años, no uno solo.
 */
public record ReporteDTO(String carrera, List<DetalleAnio> detallePorAnio) {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Carrera: ").append(carrera);
        if (detallePorAnio.isEmpty()) {
            sb.append(" (sin inscriptos)");
        }
        for (DetalleAnio d : detallePorAnio) {
            sb.append("\n").append(d);
        }
        return sb.toString();
    }

    public record DetalleAnio(Integer anio, long inscriptos, long egresados) {
        @Override
        public String toString() {
            return "  " + anio + " -> inscriptos: " + inscriptos + ", egresados: " + egresados;
        }
    }
}
