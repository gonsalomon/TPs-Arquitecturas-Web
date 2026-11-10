package org.example.orden.feignClients;

import org.example.orden.dto.request.EmitirEntradasRequestDTO;
import org.example.orden.dto.response.EntradaEmitidaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "entrada-client", url = "${servicios.entrada.url}/api/entradas")
public interface EntradaClient {

    @PostMapping()
    List<EntradaEmitidaDTO> emitir(@RequestBody EmitirEntradasRequestDTO request);
}
