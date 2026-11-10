package org.example.pago.service;

import org.example.pago.dto.request.PagoRequestDTO;
import org.example.pago.dto.response.PagoResponseDTO;
import org.example.pago.entity.Pago;
import org.example.pago.mapper.PagoMapper;
import org.example.pago.repository.PagoRepository;
import org.example.pago.utils.Estado;
import org.example.pago.utils.MetodoPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock private PagoRepository repository;


    private PagoService service;
    private final MetodoPago metodo = MetodoPago.values()[0];

    @BeforeEach
    void setUp() {
        service = new PagoService(new PagoMapper(), repository);
        lenient().when(repository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void unPagoConMontoPositivoSeAprueba() {
        when(repository.existsByIdReserva(1L)).thenReturn(false);

        PagoResponseDTO dto = service.realizarPago(new PagoRequestDTO(1L, 7L, metodo, new BigDecimal("30000")));

        assertEquals(Estado.ACEPTADO, dto.getEstado());
        assertNotNull(dto.getFecha_emision());
    }

    @Test
    void unPagoSinMontoSeRechaza() {
        when(repository.existsByIdReserva(1L)).thenReturn(false);

        PagoResponseDTO dto = service.realizarPago(new PagoRequestDTO(1L, 7L, metodo, BigDecimal.ZERO));

        assertEquals(Estado.RECHAZADO, dto.getEstado());
    }

    @Test
    void unaReservaNoSePagaDosVeces() {
        when(repository.existsByIdReserva(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.realizarPago(new PagoRequestDTO(1L, 7L, metodo, new BigDecimal("30000"))));
        verify(repository, never()).save(any());
    }

    @Test
    void elReembolsoMarcaElPagoComoReembolsado() {
        Pago pago = new Pago(1L, 7L, metodo);
        pago.setEstado(Estado.ACEPTADO);
        pago.setMonto(new BigDecimal("30000"));
        when(repository.findByIdReservaAndEstado(1L, Estado.ACEPTADO)).thenReturn(Optional.of(pago));

        PagoResponseDTO dto = service.procesarReembolso(1L);

        assertEquals(Estado.REEMBOLSADO, dto.getEstado());
        assertNotNull(pago.getFecha_reembolso());
    }

    @Test
    void noSeReembolsaUnPagoQueNoFueAprobado() {
        when(repository.findByIdReservaAndEstado(1L, Estado.ACEPTADO)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.procesarReembolso(1L));
    }
}
