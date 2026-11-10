package org.example.evento.service;

import org.example.evento.dto.request.LugarRequestDTO;
import org.example.evento.dto.request.SectorRequestDTO;
import org.example.evento.dto.response.LugarResponseDTO;
import org.example.evento.entity.Lugar;
import org.example.evento.mapper.LugarMapper;
import org.example.evento.repository.EventoRepository;
import org.example.evento.repository.LugarRepository;
import org.example.evento.utils.TipoSector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LugarServiceTest {

    @Mock private LugarRepository repository;
    @Mock private EventoRepository eventoRepository;

    private LugarService service;

    @BeforeEach
    void setUp() {
        service = new LugarService(repository, eventoRepository, new LugarMapper());
        lenient().when(repository.save(any(Lugar.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private LugarRequestDTO request(List<SectorRequestDTO> sectores) {
        return new LugarRequestDTO("Estadio Tandil", "Calle 1", "Tandil", -37.3, -59.1, sectores);
    }

    @Test
    void creaUnLugarConSectoresNumeradosYNoNumerados() {
        when(repository.existsByNombreIgnoreCase("Estadio Tandil")).thenReturn(false);

        LugarResponseDTO dto = service.save(request(List.of(
                new SectorRequestDTO("Campo", TipoSector.NO_NUMERADO, null, null, 5000),
                new SectorRequestDTO("Platea", TipoSector.NUMERADO, 20, 30, null))));

        assertEquals(2, dto.getSectores().size());
    }

    @Test
    void elNombreDelLugarEsUnico() {
        when(repository.existsByNombreIgnoreCase("Estadio Tandil")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.save(request(List.of(
                new SectorRequestDTO("Campo", TipoSector.NO_NUMERADO, null, null, 5000)))));
        verify(repository, never()).save(any());
    }

    @Test
    void unSectorNumeradoNecesitaFilasYAsientos() {
        assertThrows(IllegalArgumentException.class, () -> service.save(request(List.of(
                new SectorRequestDTO("Platea", TipoSector.NUMERADO, null, null, 600)))));
    }

    @Test
    void unSectorNoNumeradoNecesitaCapacidad() {
        assertThrows(IllegalArgumentException.class, () -> service.save(request(List.of(
                new SectorRequestDTO("Campo", TipoSector.NO_NUMERADO, null, null, null)))));
    }

    @Test
    void noSePuedenRepetirSectores() {
        assertThrows(IllegalArgumentException.class, () -> service.save(request(List.of(
                new SectorRequestDTO("Campo", TipoSector.NO_NUMERADO, null, null, 5000),
                new SectorRequestDTO("campo", TipoSector.NO_NUMERADO, null, null, 100)))));
    }

    @Test
    void noSeBorraUnLugarConEventos() {
        Lugar lugar = new Lugar();
        lugar.setIdLugar("lugar-1");
        when(repository.findById("lugar-1")).thenReturn(Optional.of(lugar));
        when(eventoRepository.existsByIdLugar("lugar-1")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.delete("lugar-1"));
        verify(repository, never()).delete(any());
    }
}
