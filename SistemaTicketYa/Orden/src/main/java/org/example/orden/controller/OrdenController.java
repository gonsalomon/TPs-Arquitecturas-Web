package org.example.orden.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orden.dto.request.CrearOrdenRequestDTO;
import org.example.orden.dto.request.PagarOrdenRequestDTO;
import org.example.orden.dto.response.OrdenResponseDTO;
import org.example.orden.service.OrdenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Coordina la compra de entradas: reserva, pago y emisión")
public class OrdenController {

    private static final String HEADER_CLIENTE = "X-Usuario-Id";

    private final OrdenService service;

    @PostMapping
    @Operation(summary = "Crea una orden de compra",
            description = "Valida el evento, reserva los lugares temporalmente y deja la orden PENDIENTE_PAGO")
    @ApiResponse(responseCode = "201", description = "Orden creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o inconsistentes con el tipo de sector")
    @ApiResponse(responseCode = "404", description = "Evento inexistente")
    @ApiResponse(responseCode = "409", description = "Evento no publicado o asiento ocupado")
    @ApiResponse(responseCode = "422", description = "Límite de 6 entradas por evento excedido")
    @ApiResponse(responseCode = "503", description = "Servicio de eventos o reservas no disponible")
    public ResponseEntity<OrdenResponseDTO> crear(
            @Parameter(description = "Id numérico del cliente autenticado", required = true)
            @RequestHeader(HEADER_CLIENTE) Long clienteId,
            @Valid @RequestBody CrearOrdenRequestDTO request) {
        OrdenResponseDTO creada = service.crear(clienteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{id}/pago")
    @Operation(summary = "Paga una orden pendiente",
            description = "Cobra el total de la orden; reintentar sobre una orden ya procesada no cobra ni emite de nuevo")
    @ApiResponse(responseCode = "200", description = "Orden CONFIRMADA o RECHAZADA, con entradas si fue confirmada")
    @ApiResponse(responseCode = "400", description = "Medio de pago inválido")
    @ApiResponse(responseCode = "403", description = "La orden no pertenece al cliente")
    @ApiResponse(responseCode = "404", description = "Orden inexistente")
    @ApiResponse(responseCode = "409", description = "La orden ya venció")
    @ApiResponse(responseCode = "503", description = "Servicio de pagos, reservas o entradas no disponible")
    public ResponseEntity<OrdenResponseDTO> pagar(
            @RequestHeader(HEADER_CLIENTE) Long clienteId,
            @PathVariable Long id,
            @Valid @RequestBody PagarOrdenRequestDTO request) {
        OrdenResponseDTO pagada = service.pagar(clienteId, id, request);
        return ResponseEntity.ok(pagada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una orden propia por id")
    @ApiResponse(responseCode = "200", description = "Detalle de la orden")
    @ApiResponse(responseCode = "403", description = "La orden no pertenece al cliente")
    @ApiResponse(responseCode = "404", description = "Orden inexistente")
    public ResponseEntity<OrdenResponseDTO> buscarPorId(
            @RequestHeader(HEADER_CLIENTE) Long clienteId,
            @PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(clienteId, id));
    }

    @GetMapping
    @Operation(summary = "Lista las órdenes del cliente, opcionalmente filtradas por evento")
    @ApiResponse(responseCode = "200", description = "Listado de órdenes del cliente")
    public ResponseEntity<List<OrdenResponseDTO>> listar(
            @RequestHeader(HEADER_CLIENTE) Long clienteId,
            @RequestParam(required = false) String eventoId) {
        return ResponseEntity.ok(service.listar(clienteId, eventoId));
    }
}
