package org.example.evento.service;

import lombok.AllArgsConstructor;
import org.example.evento.dto.request.LugarRequestDTO;
import org.example.evento.dto.request.SectorRequestDTO;
import org.example.evento.dto.response.LugarResponseDTO;
import org.example.evento.entity.Lugar;
import org.example.evento.mapper.LugarMapper;
import org.example.evento.repository.EventoRepository;
import org.example.evento.repository.LugarRepository;
import org.example.evento.utils.TipoSector;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class LugarService {
    private final LugarRepository repository;
    private final EventoRepository eventoRepository;
    private final LugarMapper mapper;

    /*-------------------------- MÉTODOS PARA EL CRUD --------------------------*/
    public LugarResponseDTO save(LugarRequestDTO request) {
        if (repository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new IllegalStateException("Ya existe un lugar con el nombre: " + request.getNombre());
        }
        validarSectores(request.getSectores());
        Lugar nuevo = repository.save(mapper.convertToEntity(request));
        return mapper.convertToDTO(nuevo);
    }

    public List<LugarResponseDTO> findAll() {
        return repository.findAll().stream().map(mapper::convertToDTO).toList();
    }

    public LugarResponseDTO findById(String idLugar) {
        return mapper.convertToDTO(buscar(idLugar));
    }

    public LugarResponseDTO findByNombre(String nombre) {
        Lugar lugar = repository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un lugar con el nombre: " + nombre));
        return mapper.convertToDTO(lugar);
    }

    public LugarResponseDTO update(String idLugar, LugarRequestDTO request) {
        Lugar lugar = buscar(idLugar);
        repository.findByNombreIgnoreCase(request.getNombre())
                .filter(otro -> !otro.getIdLugar().equals(idLugar))
                .ifPresent(otro -> { throw new IllegalStateException("Ya existe otro lugar con ese nombre"); });
        validarSectores(request.getSectores());
        lugar.setNombre(request.getNombre());
        lugar.setDireccion(request.getDireccion());
        lugar.setCiudad(request.getCiudad());
        lugar.setLatitud(request.getLatitud());
        lugar.setLongitud(request.getLongitud());
        lugar.setSectores(mapper.convertSectores(request.getSectores()));
        return mapper.convertToDTO(repository.save(lugar));
    }

    public void delete(String idLugar) {
        Lugar lugar = buscar(idLugar);
        if (eventoRepository.existsByIdLugar(idLugar)) {
            throw new IllegalStateException("No se puede borrar el lugar: tiene eventos asociados");
        }
        repository.delete(lugar);
    }

    /*-------------------------- AUXILIARES --------------------------*/
    public Lugar buscar(String idLugar) {
        return repository.findById(idLugar)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el lugar con el id: " + idLugar));
    }

    /* Numerado necesita filas y asientos; no numerado necesita capacidad. Sin nombres repetidos. */
    private void validarSectores(List<SectorRequestDTO> sectores) {
        Set<String> nombres = new HashSet<>();
        for (SectorRequestDTO s : sectores) {
            if (!nombres.add(s.getNombre().toLowerCase())) {
                throw new IllegalArgumentException("Sector repetido: " + s.getNombre());
            }
            if (s.getTipo() == TipoSector.NUMERADO && (s.getFilas() == null || s.getAsientosPorFila() == null)) {
                throw new IllegalArgumentException("El sector numerado " + s.getNombre() + " necesita filas y asientosPorFila");
            }
            if (s.getTipo() == TipoSector.NO_NUMERADO && s.getCapacidad() == null) {
                throw new IllegalArgumentException("El sector no numerado " + s.getNombre() + " necesita capacidad");
            }
        }
    }
}
