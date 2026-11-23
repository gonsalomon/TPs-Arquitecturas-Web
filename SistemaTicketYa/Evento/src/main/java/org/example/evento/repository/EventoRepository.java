package org.example.evento.repository;

import org.example.evento.entity.Evento;
import org.example.evento.utils.EstadoEvento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface EventoRepository extends MongoRepository<Evento, String> {
    List<Evento> findByEstado(EstadoEvento estado);
    boolean existsByIdLugar(String idLugar);

    List<Evento> findByEstadoAndFechaHoraAfter(String estado, LocalDateTime ahora);

    List<Evento> findByIdEventoInAndEstadoAndFechaHoraAfter(
            Collection<String> ids,
            String estado,
            LocalDateTime ahora
    );
}
