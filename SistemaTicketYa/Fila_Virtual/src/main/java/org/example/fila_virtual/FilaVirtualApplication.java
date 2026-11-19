package org.example.fila_virtual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class FilaVirtualApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilaVirtualApplication.class, args);
    }

}
