package org.example.reserva.repository;

import jakarta.persistence.LockModeType;
import org.example.reserva.entity.BloqueoSector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BloqueoSectorRepository extends JpaRepository<BloqueoSector, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BloqueoSector b WHERE b.idEvento = :idEvento AND b.sector = :sector")
    Optional<BloqueoSector> findWithLock(@Param("idEvento") String idEvento, @Param("sector") String sector);
}
