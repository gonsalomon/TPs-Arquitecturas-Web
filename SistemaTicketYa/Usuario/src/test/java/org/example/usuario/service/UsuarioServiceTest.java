package org.example.usuario.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.usuario.dto.request.UsuarioRequestDTO;
import org.example.usuario.dto.response.UsuarioResponseDTO;
import org.example.usuario.entity.Usuario;
import org.example.usuario.mapper.UsuarioMapper;
import org.example.usuario.repository.UsuarioRepository;
import org.example.usuario.utils.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository repository;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, new UsuarioMapper());
        lenient().when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private UsuarioRequestDTO request(String email) {
        return new UsuarioRequestDTO("Ana", "Pérez", email, Rol.CLIENTE);
    }

    @Test
    void creaUnUsuarioConEmailNuevo() {
        when(repository.findByEmail("ana@mail.com")).thenReturn(Optional.empty());

        UsuarioResponseDTO dto = service.save(request("ana@mail.com"));

        assertEquals("ana@mail.com", dto.getEmail());
        assertEquals(Rol.CLIENTE, dto.getRol());
    }

    @Test
    void noPermiteDosUsuariosConElMismoEmail() {
        when(repository.findByEmail("ana@mail.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(IllegalArgumentException.class, () -> service.save(request("ana@mail.com")));
        verify(repository, never()).save(any());
    }

    @Test
    void buscarUnUsuarioInexistenteFalla() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findByIdUsuario(99L));
    }

    @Test
    void actualizarCambiaLosDatos() {
        Usuario existente = new Usuario("Ana", "Pérez", "ana@mail.com", Rol.CLIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        UsuarioResponseDTO dto = service.update(1L, new UsuarioRequestDTO("Ana", "Gómez", "ana.g@mail.com", Rol.ORGANIZADOR));

        assertEquals("Gómez", dto.getApellido());
        assertEquals(Rol.ORGANIZADOR, dto.getRol());
    }

    @Test
    void borrarUnUsuarioInexistenteFalla() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).delete(any());
    }
}
