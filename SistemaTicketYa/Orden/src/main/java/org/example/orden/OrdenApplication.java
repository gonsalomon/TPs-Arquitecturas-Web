package org.example.orden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class OrdenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdenApplication.class, args);
    }

}
