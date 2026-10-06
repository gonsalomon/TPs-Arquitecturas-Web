package org.example.ejercicioIntegrador3.mapper;

import org.example.ejercicioIntegrador3.dto.CarreraDTO;
import org.example.ejercicioIntegrador3.entity.Carrera;
import org.springframework.stereotype.Component;

@Component
public class CarreraMapper {
    //si tuvira un metodo en el service que cree una carrera utilizaria este metodo
    //para convertir el objeto CarreraDTO a Carrera
    public static Carrera convertToEntity(CarreraDTO dto){
        return new Carrera(
                dto.getNombre(),
                dto.getDuracion()
        );
    }

    public CarreraDTO convertToDTO(Carrera entity){
        CarreraDTO dto = new CarreraDTO();
        dto.setIdCarrera(entity.getIdCarrera());
        dto.setNombre(entity.getNombre());
        dto.setDuracion(entity.getDuracion());
        return dto;
    }
}
/*La clase Mapper se usa para convertir un objeto de un tipo a otro, normalmente entre distintas capas de una aplicación.
Por ejemplo, en una aplicación Java con arquitectura por capas:

Base de datos
     ↓
   Entity
     ↓  Mapper
    DTO
     ↓
   Controller

¿Por qué no convertir directamente?
Porque ayuda a separar responsabilidades.

Entity → representa cómo se guarda la información en la base de datos.

DTO → representa qué información querés transportar o exponer.

Mapper → sabe cómo transformar uno en otro.

Service → contiene la lógica de negocio.

Controller → maneja las peticiones HTTP.
*/