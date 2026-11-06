package org.example.reserva.feignClients;

import org.example.reserva.dto.request.UsuarioExternoDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary // Le dice a Spring que inyecte esta clase en lugar del FeignClient real de Usuarios
@Component
public class UsuarioClientMock implements UsuarioClient {

    @Override
    public UsuarioExternoDTO obtenerUsuario(Long id) { // Nota: Si en tu interfaz definiste el id como Long, cámbialo aquí
        UsuarioExternoDTO usuario = new UsuarioExternoDTO();

        // Asignamos el mismo ID que entra por parámetro para que la validación posterior coincida
        usuario.setId(id);
        usuario.setNombre("Usuario Simulado");

        // Si tu UsuarioExternoDTO tiene más campos (ej. email, dni), puedes setearlos aquí con datos falsos

        return usuario;
    }
}
