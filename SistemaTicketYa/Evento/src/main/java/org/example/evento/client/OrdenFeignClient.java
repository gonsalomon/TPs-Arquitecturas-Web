package org.example.evento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "orden-client", url = "http://localhost:8087/api/ordenes")
public interface OrdenFeignClient {
    @PutMapping("/{idEvento}/cancelacion")
    void cancelacionEvento(@PathVariable String idEvento);
}
