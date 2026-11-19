package org.example.usuario.service;

import lombok.AllArgsConstructor;
import org.example.usuario.entity.Authority;
import org.example.usuario.repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class RolService {
    private final RolRepository repository;

    public void create(List<String> roles) {
        for (String rol : roles) {
            repository.save(new Authority(rol));
        }
    }

}
