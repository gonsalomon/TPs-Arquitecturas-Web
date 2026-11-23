package org.example.entrada.service;

import lombok.RequiredArgsConstructor;
import org.example.entrada.utils.EstadoEntrada;
import org.example.entrada.repository.EntradaRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntradaRecomendacionService {

    private final EntradaRepository entradaRepository;

    // Ajustar si tu enum usa ANULADA o REEMBOLSADA
    private static final EstadoEntrada ESTADO_EXCLUIDO = EstadoEntrada.REINTEGRO;

    public List<String> obtenerCoCompradosPorEvento(String eventoId, int limit) {
        return entradaRepository.findEventosCoComprados(
                eventoId,
                ESTADO_EXCLUIDO,
                PageRequest.of(0, Math.max(1, limit))
        );
    }

    public List<String> obtenerRecomendadosPorHistorial(Long clienteId, int limit) {
        return entradaRepository.findRecomendadosPorHistorialCliente(
                clienteId,
                ESTADO_EXCLUIDO,
                PageRequest.of(0, Math.max(1, limit))
        );
    }

    public List<String> obtenerEventosComprados(Long clienteId) {
        return entradaRepository.findEventosCompradosPorCliente(clienteId, ESTADO_EXCLUIDO);
    }
}
