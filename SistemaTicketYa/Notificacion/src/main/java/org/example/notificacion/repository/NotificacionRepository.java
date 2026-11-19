package org.example.notificacion.repository;

import org.example.notificacion.entity.Notificacion;
import org.example.notificacion.utils.NotificacionTipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository <Notificacion,Long> {
    //Spring Data JPA ya me proporciona save()
    List<Notificacion> findByTipo(NotificacionTipo tipo);
}
