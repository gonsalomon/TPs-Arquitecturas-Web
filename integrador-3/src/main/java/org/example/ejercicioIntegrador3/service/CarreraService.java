package org.example.ejercicioIntegrador3.service;


import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.CarreraCantInscriptosDTO;
import org.example.ejercicioIntegrador3.dto.CarreraDTO;
import org.example.ejercicioIntegrador3.dto.ConteoPorAnioDTO;
import org.example.ejercicioIntegrador3.dto.ReporteCarreraDTO;
import org.example.ejercicioIntegrador3.entity.Carrera;
import org.example.ejercicioIntegrador3.mapper.CarreraMapper;
import org.example.ejercicioIntegrador3.repository.CarreraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@AllArgsConstructor
@Service
public class CarreraService {
    private final CarreraRepository carreraRepository;
    private final CarreraMapper mapper;

    @Transactional(readOnly = true)
    public CarreraDTO findById(Integer id) {
        Carrera c = carreraRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND,"No se encontró una carrera con el id: " + id));
        return mapper.convertToDTO(c);
    }

    @Transactional(readOnly = true)
    public List<CarreraCantInscriptosDTO> findConInscriptosOrdenadasPorCantidad() {
        return carreraRepository.findConInscriptosOrdenadasPorCantidad();
    }

    /* h) reporte de las carreras con los inscriptos y egresados de cada año.
     * Los inscriptos salen del año de inscripción y los egresados del año de graduación, que son
     * dos columnas distintas, así que cada conteo se resuelve con su propia consulta JPQL y acá
     * solamente se combinan los dos resultados en una fila por carrera y año.
     * El TreeMap de afuera deja las carreras en orden alfabético y el de adentro, los años en
     * orden cronológico.
     */
    @Transactional(readOnly = true)
    public List<ReporteCarreraDTO> generarReporte() {
        Map<String, Map<Integer, ReporteCarreraDTO>> reporte = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // Se arranca con todas las carreras para que figuren también las que no tienen inscriptos.
        for (Carrera carrera : carreraRepository.findAll()) {
            reporte.put(carrera.getNombre(), new TreeMap<>());
        }
        for (ConteoPorAnioDTO fila : carreraRepository.contarInscriptosPorCarreraYAnio()) {
            buscarFila(reporte, fila).setInscriptos(fila.getCantidad());
        }
        for (ConteoPorAnioDTO fila : carreraRepository.contarEgresadosPorCarreraYAnio()) {
            buscarFila(reporte, fila).setEgresados(fila.getCantidad());
        }

        List<ReporteCarreraDTO> resultado = new ArrayList<>();
        for (Map<Integer, ReporteCarreraDTO> aniosDeLaCarrera : reporte.values()) {
            resultado.addAll(aniosDeLaCarrera.values());
        }
        return resultado;
    }

    // Busca la fila de esa carrera y ese año, y si todavía no existe la crea en cero.
    private ReporteCarreraDTO buscarFila(Map<String, Map<Integer, ReporteCarreraDTO>> reporte,
                                         ConteoPorAnioDTO fila) {
        return reporte.computeIfAbsent(fila.getCarrera(), nombre -> new TreeMap<>())
                .computeIfAbsent(fila.getAnio(), anio -> new ReporteCarreraDTO(fila.getCarrera(), anio));
    }

}
