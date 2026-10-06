package org.example.ejercicioIntegrador3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication //Spring puede escanear automáticamente todos los subpackages de org. no requiere que le indiquemos uno por uno
//@EntityScan("org.example.ejercicioIntegrador3.entity")
//@EnableJpaRepositories("org.example.ejercicioIntegrador3.repository")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
/*al cambiar la clase principal que ahora se encuentra en: package org.example.ejercicioIntegrador3;
y los paquetes están debajo de ella:
org.example.ejercicioIntegrador3.entity
org.example.ejercicioIntegrador3.repository
org.example.ejercicioIntegrador3.service
org.example.ejercicioIntegrador3.controller
Spring Boot hace el escaneo automáticamente mediante @SpringBootApplication. por lo cual no requiere las notaciones
@EntityScan ni @EnableJpaRepositories*/