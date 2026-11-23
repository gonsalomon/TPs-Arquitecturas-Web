package org.example.orden.feignClients;

import org.example.orden.dto.request.EmitirEntradasRequestDTO;
import org.example.orden.dto.response.EntradaDTO;
import org.example.orden.dto.response.EntradaEmitidaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "entrada-client", url = "${servicios.entrada.url}/api/entradas")
public interface EntradaClient {

    @PostMapping()
    List<EntradaEmitidaDTO> emitir(@RequestBody EmitirEntradasRequestDTO request);

    @GetMapping("/{idOrden}/filter")
    List<EntradaDTO> getEntradas(@RequestParam String estado, @PathVariable Long idOrden);

}
