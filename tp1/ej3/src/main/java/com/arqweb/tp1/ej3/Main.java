package com.arqweb.tp1.ej3;

import com.arqweb.tp1.ej3.dao.PersonaDAO;
import com.arqweb.tp1.ej3.dao.PersonaDAODerby;
import com.arqweb.tp1.ej3.dao.PersonaDAOMySQL;
import com.arqweb.tp1.ej3.model.Persona;
import com.arqweb.tp1.ej3.service.PersonaService;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // Para probar contra MySQL en vez de Derby, esta es la única línea que hay que cambiar:
        PersonaDAO dao = new PersonaDAODerby();
        // PersonaDAO dao = new PersonaDAOMySQL();

        PersonaService service = new PersonaService(dao);

        service.agregarPersona(new Persona(1, "Ana", 28));
        service.agregarPersona(new Persona(2, "Bruno", 34));
        service.agregarPersona(new Persona(3, "Carla", 22));
        service.agregarPersona(new Persona(4, "Diego", 41));

        List<Persona> personas = service.obtenerTodasLasPersonas();
        System.out.println("=== Personas cargadas ===");
        personas.forEach(System.out::println);
    }
}
