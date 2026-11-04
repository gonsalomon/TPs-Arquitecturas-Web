package org.example.notificacion.repository;

import org.example.notificacion.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository <Notificacion,Long> {
    //Spring Data JPA ya me proporciona save()
}
