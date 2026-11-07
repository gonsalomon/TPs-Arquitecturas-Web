package org.example.evento.repository;

import org.example.evento.entity.Evento;
import org.example.evento.utils.EstadoEvento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends MongoRepository<Evento, String> {
    List<Evento> findByEstado(EstadoEvento estado);
    boolean existsByIdLugar(String idLugar);
}
