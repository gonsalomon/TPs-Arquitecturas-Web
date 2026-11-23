package org.example.evento.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.evento.client.EntradaClient;
import org.example.evento.client.UsuarioFeingClient;
import org.example.evento.dto.response.EventoRecomendadoDTO;
import org.example.evento.dto.request.PreferenciasExternasDTO;
import org.example.evento.entity.Evento;
import org.example.evento.repository.EventoRepository;
import org.example.evento.repository.LugarRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecomendacionService {

    private static final String ESTADO_PUBLICADO = "PUBLICADO";

    private final EventoRepository eventoRepository;
    private final LugarRepository lugarRepository;
    private final UsuarioFeingClient usuarioClient;
    private final EntradaClient entradaClient;

    /**
     * "Quienes compraron entradas para este evento también compraron para..."
     * Incluye fallback por tipo y participantes cuando aún no existen co-compras.
     */
    public List<EventoRecomendadoDTO> obtenerEventosSimilares(String eventoId, int limit) {
        Evento eventoBase = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();
        List<String> idsCoComprados = llamarSeguro(
                () -> entradaClient.obtenerCoCompradosPorEvento(eventoId, limit),
                List.of()
        );

        List<Evento> seleccionados = new ArrayList<>();
        Set<String> idsUsados = new HashSet<>();
        idsUsados.add(eventoId);

        // 1. Priorizar eventos co-comprados preservando el ranking de frecuencia
        if (!idsCoComprados.isEmpty()) {
            Map<String, Evento> mapaEventos = eventoRepository
                    .findByIdEventoInAndEstadoAndFechaHoraAfter(idsCoComprados, ESTADO_PUBLICADO, ahora)
                    .stream()
                    .collect(Collectors.toMap(Evento::getIdEvento, Function.identity()));

            for (String id : idsCoComprados) {
                Evento ev = mapaEventos.get(id);
                if (ev != null && idsUsados.add(ev.getIdEvento())) {
                    seleccionados.add(ev);
                }
            }
        }

        // 2. Fallback (cold start): completar con eventos publicados del mismo tipo o artistas
        if (seleccionados.size() < limit) {
            Set<String> artistasBase = normalizar(eventoBase.getParticipantes());
            String tipoBase = eventoBase.getTipo() != null ? eventoBase.getTipo().toString() : "";

            List<Evento> candidatos = eventoRepository.findByEstadoAndFechaHoraAfter(ESTADO_PUBLICADO, ahora);
            for (Evento candidato : candidatos) {
                if (seleccionados.size() >= limit) break;
                if (!idsUsados.contains(candidato.getIdEvento())) {
                    boolean mismoTipo = candidato.getTipo() != null
                            && candidato.getTipo().toString().equalsIgnoreCase(tipoBase);
                    boolean comparteArtista = comparteAlguno(normalizar(candidato.getParticipantes()), artistasBase);
                    if (mismoTipo || comparteArtista) {
                        idsUsados.add(candidato.getIdEvento());
                        seleccionados.add(candidato);
                    }
                }
            }
        }

        Map<String, String> ciudadesPorLugar = obtenerMapaCiudades(seleccionados);
        Set<String> setCoComprados = new HashSet<>(idsCoComprados);

        return seleccionados.stream()
                .limit(limit)
                .map(ev -> {
                    boolean esCoCompra = setCoComprados.contains(ev.getIdEvento());
                    String motivo = esCoCompra
                            ? "Quienes compraron entradas para este evento también compraron para este"
                            : "Evento similar por tipo o participantes";
                    return toDTO(ev, ciudadesPorLugar.get(ev.getIdLugar()), esCoCompra ? 5 : 2, List.of(motivo));
                })
                .toList();
    }

    /**
     * Recomendaciones personalizadas combinando historial colaborativo y preferencias del perfil.
     */
    public List<EventoRecomendadoDTO> obtenerRecomendacionesParaCliente(Long clienteId, int limit) {
        PreferenciasExternasDTO prefs = llamarSeguro(
                () -> usuarioClient.obtenerPreferencias(clienteId),
                new PreferenciasExternasDTO()
        );
        List<String> coComprados = llamarSeguro(
                () -> entradaClient.obtenerRecomendadosPorHistorial(clienteId, limit * 2),
                List.of()
        );
        Set<String> yaComprados = new HashSet<>(llamarSeguro(
                () -> entradaClient.obtenerEventosComprados(clienteId),
                List.of()
        ));

        Set<String> setCoComprados = new HashSet<>(coComprados);
        Set<String> tiposPreferidos = normalizar(prefs.getTiposEvento());
        Set<String> artistasPreferidos = normalizar(prefs.getArtistasFavoritos());
        String ciudadPreferida = prefs.getCiudad() != null
                ? prefs.getCiudad().trim().toLowerCase(Locale.ROOT)
                : "";

        List<Evento> candidatos = eventoRepository.findByEstadoAndFechaHoraAfter(ESTADO_PUBLICADO, LocalDateTime.now())
                .stream()
                .filter(ev -> !yaComprados.contains(ev.getIdEvento()))
                .toList();

        Map<String, String> ciudadesPorLugar = obtenerMapaCiudades(candidatos);
        List<EventoRecomendadoDTO> recomendados = new ArrayList<>();

        for (Evento ev : candidatos) {
            int puntaje = 0;
            List<String> motivos = new ArrayList<>();

            // +5 por historial colaborativo
            if (setCoComprados.contains(ev.getIdEvento())) {
                puntaje += 5;
                motivos.add("Quienes compraron eventos como los tuyos también eligieron este");
            }

            // +4 por artista/participante favorito
            if (comparteAlguno(normalizar(ev.getParticipantes()), artistasPreferidos)) {
                puntaje += 4;
                motivos.add("Participa uno de tus artistas favoritos");
            }

            // +2 por tipo de evento preferido
            if (ev.getTipo() != null
                    && tiposPreferidos.contains(ev.getTipo().toString().trim().toLowerCase(Locale.ROOT))) {
                puntaje += 2;
                motivos.add("Coincide con tus tipos de evento preferidos");
            }

            // +2 por ciudad preferida
            String ciudadEvento = ciudadesPorLugar.get(ev.getIdLugar());
            if (!ciudadPreferida.isEmpty() && ciudadEvento != null
                    && ciudadEvento.trim().toLowerCase(Locale.ROOT).equals(ciudadPreferida)) {
                puntaje += 2;
                motivos.add("Se realiza en tu ciudad (" + ciudadEvento + ")");
            }

            if (puntaje > 0) {
                recomendados.add(toDTO(ev, ciudadEvento, puntaje, motivos));
            }
        }

        return recomendados.stream()
                .sorted(Comparator
                        .comparingInt(EventoRecomendadoDTO::getPuntaje).reversed()
                        .thenComparing(EventoRecomendadoDTO::getFechaHora))
                .limit(limit)
                .toList();
    }

    private Map<String, String> obtenerMapaCiudades(List<Evento> eventos) {
        Set<String> lugarIds = eventos.stream()
                .map(Evento::getIdLugar)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> mapa = new HashMap<>();
        lugarRepository.findAllById(lugarIds)
                .forEach(lugar -> mapa.put(lugar.getIdLugar(), lugar.getCiudad()));
        return mapa;
    }

    private EventoRecomendadoDTO toDTO(Evento ev, String ciudad, int puntaje, List<String> motivos) {
        return EventoRecomendadoDTO.builder()
                .eventoId(ev.getIdEvento())
                .nombre(ev.getNombre())
                .tipo(ev.getTipo() != null ? ev.getTipo().toString() : null)
                .ciudad(ciudad)
                .fechaHora(ev.getFechaHora())
                .participantes(ev.getParticipantes())
                .puntaje(puntaje)
                .motivos(motivos)
                .build();
    }

    private Set<String> normalizar(Collection<String> valores) {
        if (valores == null) return Set.of();
        return valores.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private boolean comparteAlguno(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        return a.stream().anyMatch(b::contains);
    }

    private <T> T llamarSeguro(Supplier<T> llamada, T fallback) {
        try {
            T resultado = llamada.get();
            return resultado != null ? resultado : fallback;
        } catch (Exception ex) {
            log.warn("Error consultando microservicio remoto para recomendaciones: {}", ex.getMessage());
            return fallback;
        }
    }
}
