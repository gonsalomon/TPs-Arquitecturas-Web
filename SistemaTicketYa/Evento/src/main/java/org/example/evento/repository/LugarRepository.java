package org.example.evento.repository;

import org.example.evento.entity.Lugar;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LugarRepository extends MongoRepository<Lugar, String> {
    Optional<Lugar> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
