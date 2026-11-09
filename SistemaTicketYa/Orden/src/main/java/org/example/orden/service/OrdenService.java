package org.example.orden.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.example.orden.dto.request.*;
import org.example.orden.dto.response.*;
import org.example.orden.entity.ItemOrden;
import org.example.orden.entity.Orden;
import org.example.orden.exception.*;
import org.example.orden.feignClients.EntradaClient;
import org.example.orden.feignClients.EventoClient;
import org.example.orden.feignClients.PagoClient;
import org.example.orden.feignClients.ReservaClient;
import org.example.orden.mapper.OrdenMapper;
import org.example.orden.repository.OrdenRepository;
import org.example.orden.utils.ConversorAsientos;
import org.example.orden.utils.EstadoOrden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private static final String PUBLICADO = "PUBLICADO";
    private static final String NUMERADO = "NUMERADO";
    private static final String RECHAZADO = "RECHAZADO";

    private final OrdenRepository ordenRepository;
    private final OrdenMapper mapper;
    private final EventoClient eventoClient;
    private final ReservaClient reservaClient;
    private final PagoClient pagoClient;
    private final EntradaClient entradaClient;

    @Value("${orden.limite-entradas-por-evento}")
    private int limiteEntradasPorEvento;

    // ---------- Crear orden ----------

    @Transactional
    public OrdenResponseDTO crear(Long clienteId, CrearOrdenRequestDTO request) {
        EventoExternoDTO evento = obtenerEvento(request.getEventoId());
        if (!PUBLICADO.equals(evento.getEstado())) {
            throw new EventoNoDisponibleException("El evento no está publicado");
        }

        PrecioSectorExternoDTO precioSector = evento.getPrecios().stream()
                .filter(p -> p.getSector().equalsIgnoreCase(request.getSector()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El sector no tiene precio definido para este evento"));

        LugarExternoDTO lugar = obtenerLugar(evento.getIdLugar());
        SectorExternoDTO sector = lugar.getSectores().stream()
                .filter(s -> s.getNombre().equalsIgnoreCase(request.getSector()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El sector no existe en el lugar del evento"));

        Orden orden = new Orden(clienteId, request.getEventoId(), sector.getNombre(), evento.getFechaHora());

        List<Long> numerosDeAsiento = NUMERADO.equalsIgnoreCase(sector.getTipo())
                ? armarItemsNumerados(orden, request, sector, precioSector.getPrecio())
                : armarItemsNoNumerados(orden, request, precioSector.getPrecio());

        long activasDelCliente = ordenRepository.contarEntradasActivasPorClienteYEvento(clienteId, request.getEventoId());
        if (activasDelCliente + orden.getItems().size() > limiteEntradasPorEvento) {
            throw new LimiteEntradasExcedidoException(
                    "Supera el límite de " + limiteEntradasPorEvento + " entradas por evento");
        }

        try {
            ordenRepository.saveAndFlush(orden);
        } catch (DataIntegrityViolationException e) {
            throw new AsientoNoDisponibleException("Uno o más asientos ya están ocupados por otra orden");
        }

        ReservaExternaDTO reserva = reservarEnReserva(orden, numerosDeAsiento);

        orden.setIdReserva(reserva.getIdReserva());
        orden.setFechaVencimiento(reserva.getFechaVencimiento());
        ordenRepository.save(orden);

        return mapper.convertToDTO(orden);
    }

    private List<Long> armarItemsNumerados(Orden orden, CrearOrdenRequestDTO request,
                                            SectorExternoDTO sector, Double precioUnitario) {
        if (request.getAsientos() == null || request.getAsientos().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar los asientos para un sector numerado");
        }
        if (request.getCantidad() != null) {
            throw new IllegalArgumentException("No debe indicar cantidad para un sector numerado");
        }

        Set<String> vistos = new HashSet<>();
        List<Long> numeros = new ArrayList<>();
        for (AsientoRequestDTO a : request.getAsientos()) {
            if (a.getFila() == null || a.getAsiento() == null) {
                throw new IllegalArgumentException("Fila y asiento son obligatorios en un sector numerado");
            }
            if (a.getFila() < 1 || a.getFila() > sector.getFilas()
                    || a.getAsiento() < 1 || a.getAsiento() > sector.getAsientosPorFila()) {
                throw new IllegalArgumentException("Asiento fuera de rango para el sector");
            }
            if (!vistos.add(a.getFila() + "-" + a.getAsiento())) {
                throw new IllegalArgumentException("Hay asientos repetidos en el pedido");
            }

            int numero = ConversorAsientos.numeroDeAsiento(a.getFila(), a.getAsiento(), sector.getAsientosPorFila());
            orden.agregarItem(new ItemOrden(a.getFila(), a.getAsiento(), numero, BigDecimal.valueOf(precioUnitario)));
            numeros.add((long) numero);
        }
        return numeros;
    }

    private List<Long> armarItemsNoNumerados(Orden orden, CrearOrdenRequestDTO request, Double precioUnitario) {
        if (request.getAsientos() != null && !request.getAsientos().isEmpty()) {
            throw new IllegalArgumentException("No debe indicar asientos para un sector no numerado");
        }
        if (request.getCantidad() == null || request.getCantidad() < 1) {
            throw new IllegalArgumentException("Debe indicar una cantidad válida para un sector no numerado");
        }
        for (int i = 0; i < request.getCantidad(); i++) {
            orden.agregarItem(new ItemOrden(null, null, null, BigDecimal.valueOf(precioUnitario)));
        }
        return null;
    }

    private ReservaExternaDTO reservarEnReserva(Orden orden, List<Long> numerosDeAsiento) {
        try {
            ReservaCrearRequestDTO reservaRequest = new ReservaCrearRequestDTO(
                    orden.getClienteId(), orden.getEventoId(), orden.getSector(), numerosDeAsiento);
            return reservaClient.crearReserva(reservaRequest);
        } catch (IllegalStateException | IllegalArgumentException e) {
            rechazarPorFalloDeReserva(orden);
            throw new AsientoNoDisponibleException("Los lugares pedidos no están disponibles");
        } catch (FeignException e) {
            rechazarPorFalloDeReserva(orden);
            throw new ServicioNoDisponibleException("El servicio de reservas no está disponible");
        }
    }

    private void rechazarPorFalloDeReserva(Orden orden) {
        orden.cambiarEstado(EstadoOrden.RECHAZADA);
        orden.liberarClaves();
        ordenRepository.save(orden);
    }

    // ---------- Pagar orden ----------

    @Transactional
    public OrdenResponseDTO pagar(Long clienteId, Long idOrden, PagarOrdenRequestDTO request) {
        try {
            Orden orden = buscarPropia(clienteId, idOrden);
            return despacharSegunEstado(orden, request);
        } catch (OptimisticLockingFailureException e) {
            // Dos pagos simultáneos de la misma orden: el que pierde la carrera relee el
            // estado ya actualizado por el otro y lo devuelve, sin reintentar el cobro.
            Orden actual = buscarPropia(clienteId, idOrden);
            return mapper.convertToDTO(actual);
        }
    }

    private OrdenResponseDTO despacharSegunEstado(Orden orden, PagarOrdenRequestDTO request) {
        return switch (orden.getEstado()) {
            case RECHAZADA -> mapper.convertToDTO(orden);
            case CONFIRMADA -> reintentarEmision(orden);
            case VENCIDA -> throw new OrdenVencidaException("La orden ya venció");
            case PAGO_APROBADO -> reintentarEmision(orden);
            case PENDIENTE_PAGO -> procesarPago(orden, request);
        };
    }

    private OrdenResponseDTO procesarPago(Orden orden, PagarOrdenRequestDTO request) {
        if (orden.getFechaVencimiento() != null && LocalDateTime.now().isAfter(orden.getFechaVencimiento())) {
            liberarReservaSilenciosamente(orden.getIdReserva());
            orden.cambiarEstado(EstadoOrden.VENCIDA);
            orden.liberarClaves();
            ordenRepository.save(orden);
            throw new OrdenVencidaException("La reserva venció, no se realizó ningún cobro");
        }

        orden.setMedioPago(request.getMedioPago());

        PagoExternoDTO pago;
        try {
            PagoCrearRequestDTO pagoRequest = new PagoCrearRequestDTO(
                    orden.getIdReserva(), orden.getClienteId(), request.getMedioPago().name(), orden.getTotal());
            pago = pagoClient.realizarPago(pagoRequest);
        } catch (IllegalStateException e) {
            // 409 de Pago hoy agrupa "ya pagada", "vencida" y "límite excedido" (ver design.md,
            // decisión 9). Orden ya validó vencimiento y no vuelve a cobrar: lo trata como
            // resultado incierto y no asume ni éxito ni rechazo.
            ordenRepository.save(orden);
            throw new ServicioNoDisponibleException(
                    "No se pudo confirmar el resultado del pago, reintentá en unos segundos");
        } catch (FeignException e) {
            ordenRepository.save(orden);
            throw new ServicioNoDisponibleException("El servicio de pagos no está disponible");
        }

        orden.setIdPago(pago.getIdPago());

        if (RECHAZADO.equals(pago.getEstado())) {
            liberarReservaSilenciosamente(orden.getIdReserva());
            orden.cambiarEstado(EstadoOrden.RECHAZADA);
            orden.liberarClaves();
            ordenRepository.save(orden);
            return mapper.convertToDTO(orden);
        }

        orden.cambiarEstado(EstadoOrden.PAGO_APROBADO);
        ordenRepository.save(orden);

        confirmarReserva(orden.getIdReserva());

        return reintentarEmision(orden);
    }

    private OrdenResponseDTO reintentarEmision(Orden orden) {
        List<EntradaEmitidaDTO> entradas;
        try {
            entradas = entradaClient.emitir(construirSolicitudEmision(orden));
        } catch (FeignException e) {
            ordenRepository.save(orden);
            throw new ServicioNoDisponibleException(
                    "El pago fue aprobado pero la emisión de entradas falló, reintentá el pago");
        }

        orden.cambiarEstado(EstadoOrden.CONFIRMADA);
        ordenRepository.save(orden);

        OrdenResponseDTO dto = mapper.convertToDTO(orden);
        dto.setEntradas(entradas);
        return dto;
    }

    private EmitirEntradasRequestDTO construirSolicitudEmision(Orden orden) {
        List<ItemEmitirDTO> items = orden.getItems().stream()
                .map(i -> new ItemEmitirDTO(orden.getSector(), i.getFila(), i.getAsiento(), i.getPrecioUnitario()))
                .collect(Collectors.toList());
        return new EmitirEntradasRequestDTO(
                orden.getIdOrden(), orden.getEventoId(), orden.getClienteId(), orden.getFechaEvento(), items);
    }

    private void confirmarReserva(Long idReserva) {
        try {
            reservaClient.actualizarEstado(idReserva, "CONFIRMADA");
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("No se pudo confirmar la reserva");
        }
    }

    private void liberarReservaSilenciosamente(Long idReserva) {
        if (idReserva == null) {
            return;
        }
        try {
            reservaClient.actualizarEstado(idReserva, "LIBERADA");
        } catch (FeignException ignored) {
            // liberar una reserva ya liberada o con el servicio caído no debe interrumpir a Orden
        }
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public OrdenResponseDTO buscarPorId(Long clienteId, Long idOrden) {
        return mapper.convertToDTO(buscarPropia(clienteId, idOrden));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listar(Long clienteId, String eventoId) {
        List<Orden> ordenes = (eventoId == null || eventoId.isBlank())
                ? ordenRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId)
                : ordenRepository.findByClienteIdAndEventoIdOrderByFechaCreacionDesc(clienteId, eventoId);
        return ordenes.stream().map(mapper::convertToDTO).collect(Collectors.toList());
    }

    private Orden buscarPropia(Long clienteId, Long idOrden) {
        Orden orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la orden " + idOrden));
        if (!orden.getClienteId().equals(clienteId)) {
            throw new AccesoDenegadoException("La orden no pertenece al cliente");
        }
        return orden;
    }

    // ---------- Clientes externos (Evento) ----------

    private EventoExternoDTO obtenerEvento(String eventoId) {
        try {
            return eventoClient.obtenerEvento(eventoId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("El servicio de eventos no está disponible");
        }
    }

    private LugarExternoDTO obtenerLugar(String idLugar) {
        try {
            return eventoClient.obtenerLugar(idLugar);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("El servicio de eventos no está disponible");
        }
    }
}
