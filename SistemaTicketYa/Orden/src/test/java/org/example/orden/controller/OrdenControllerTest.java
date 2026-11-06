package org.example.orden.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orden.dto.request.CrearOrdenRequestDTO;
import org.example.orden.exception.AccesoDenegadoException;
import org.example.orden.exception.LimiteEntradasExcedidoException;
import org.example.orden.exception.ServicioNoDisponibleException;
import org.example.orden.service.OrdenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
class OrdenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrdenService service;

    private CrearOrdenRequestDTO requestValido() {
        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO();
        request.setEventoId("evt-1");
        request.setSector("Platea");
        return request;
    }

    @Test
    void faltaElHeaderDeClienteDevuelve400() throws Exception {
        mockMvc.perform(post("/api/ordenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ordenAjenaDevuelve403() throws Exception {
        when(service.buscarPorId(eq(7L), eq(1L)))
                .thenThrow(new AccesoDenegadoException("La orden no pertenece al cliente"));

        mockMvc.perform(get("/api/ordenes/1").header("X-Usuario-Id", "7"))
                .andExpect(status().isForbidden());
    }

    @Test
    void ordenInexistenteDevuelve404() throws Exception {
        when(service.buscarPorId(eq(7L), eq(1L)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "no existe"));

        mockMvc.perform(get("/api/ordenes/1").header("X-Usuario-Id", "7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void limiteDeEntradasExcedidoDevuelve422() throws Exception {
        when(service.crear(eq(7L), any())).thenThrow(new LimiteEntradasExcedidoException("límite"));

        mockMvc.perform(post("/api/ordenes")
                        .header("X-Usuario-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void medioDePagoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/ordenes/1/pago")
                        .header("X-Usuario-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medioPago\": \"BITCOIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void servicioCaidoDevuelve503() throws Exception {
        when(service.crear(eq(7L), any())).thenThrow(new ServicioNoDisponibleException("caído"));

        mockMvc.perform(post("/api/ordenes")
                        .header("X-Usuario-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isServiceUnavailable());
    }
}
