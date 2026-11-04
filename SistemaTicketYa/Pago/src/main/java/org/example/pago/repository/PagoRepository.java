package org.example.pago.repository;

import org.example.pago.dto.response.PagoResponseDTO;
import org.example.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    // 1. Buscar un pago específico por su ID
    @Query(value = "SELECT * FROM pago WHERE id = :idPago", nativeQuery = true)
    PagoResponseDTO buscarPago(@Param("idPago") Long idPago);

    boolean existsByIdReserva(Long idReserva);

}
