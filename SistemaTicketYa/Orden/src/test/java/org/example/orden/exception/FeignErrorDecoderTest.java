package org.example.orden.exception;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FeignErrorDecoderTest {

    private final FeignErrorDecoder decoder = new FeignErrorDecoder();

    private Response responseConStatus(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        return Response.builder()
                .status(status)
                .reason("test")
                .request(request)
                .headers(Collections.emptyMap())
                .build();
    }

    @Test
    void status400SeTraduceAIllegalArgumentException() {
        Exception resultado = decoder.decode("Cliente#metodo()", responseConStatus(400));
        assertInstanceOf(IllegalArgumentException.class, resultado);
    }

    @Test
    void status404SeTraduceAResponseStatusException() {
        Exception resultado = decoder.decode("Cliente#metodo()", responseConStatus(404));
        assertInstanceOf(org.springframework.web.server.ResponseStatusException.class, resultado);
    }

    @Test
    void status409SeTraduceAIllegalStateException() {
        Exception resultado = decoder.decode("Cliente#metodo()", responseConStatus(409));
        assertInstanceOf(IllegalStateException.class, resultado);
    }

    @Test
    void status500SeTraduceAServicioNoDisponibleException() {
        Exception resultado = decoder.decode("Cliente#metodo()", responseConStatus(500));
        assertInstanceOf(ServicioNoDisponibleException.class, resultado);
    }
}
