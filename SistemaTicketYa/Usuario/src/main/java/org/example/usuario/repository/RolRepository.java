package org.example.usuario.repository;

import org.example.usuario.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Authority, String> {
}
