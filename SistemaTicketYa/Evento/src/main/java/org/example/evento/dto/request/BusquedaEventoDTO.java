package org.example.evento.dto.request;

import org.example.evento.utils.OrdenBusqueda;
import org.example.evento.utils.TipoEvento;

import java.time.LocalDate;

/* Filtros de la busqueda (punto 9). Todos son opcionales.
 * Es un record: equals/hashCode por valor, por eso sirve como clave del cache. */
public record BusquedaEventoDTO(
        TipoEvento tipo,
        Long idOrganizador,
        String participante,
        String ciudad,
        LocalDate desde,
        LocalDate hasta,
        String texto,
        OrdenBusqueda orden
) {
    public BusquedaEventoDTO {
        participante = limpiar(participante);
        ciudad = limpiar(ciudad);
        texto = limpiar(texto);
        if (orden == null) orden = OrdenBusqueda.FECHA;
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
    }

    private static String limpiar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
