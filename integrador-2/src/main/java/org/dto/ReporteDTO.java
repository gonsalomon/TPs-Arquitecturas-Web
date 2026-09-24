package org.dto;

import java.util.List;

/**
 * Punto 3: reporte de carreras con inscriptos y egresados por año.
 * Cada instancia representa una carrera junto con el detalle, año por año,
 * de cuántos estudiantes se inscribieron y cuántos egresaron.
 */
public class ReporteDTO {

    private String carrera;
    private List<DetalleAnio> detallePorAnio;

    public ReporteDTO(String carrera, List<DetalleAnio> detallePorAnio) {
        this.carrera = carrera;
        this.detallePorAnio = detallePorAnio;
    }

    public String getCarrera() {
        return carrera;
    }

    public List<DetalleAnio> getDetallePorAnio() {
        return detallePorAnio;
    }

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

    /** Detalle de un año puntual para una carrera. */
    public static class DetalleAnio {
        private final Integer anio;
        private final long inscriptos;
        private final long egresados;

        public DetalleAnio(Integer anio, long inscriptos, long egresados) {
            this.anio = anio;
            this.inscriptos = inscriptos;
            this.egresados = egresados;
        }

        public Integer getAnio() {
            return anio;
        }

        public long getInscriptos() {
            return inscriptos;
        }

        public long getEgresados() {
            return egresados;
        }

        @Override
        public String toString() {
            return "  " + anio + " -> inscriptos: " + inscriptos + ", egresados: " + egresados;
        }
    }
}
