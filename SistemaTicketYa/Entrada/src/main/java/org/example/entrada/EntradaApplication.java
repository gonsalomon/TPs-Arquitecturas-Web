package org.example.entrada;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class EntradaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntradaApplication.class, args);
    }

}
