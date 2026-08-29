package com.arqweb.tp1.ej3.service;

import com.arqweb.tp1.ej3.dao.PersonaDAO;
import com.arqweb.tp1.ej3.model.Persona;

import java.sql.SQLException;
import java.util.List;

/**
 * Capa de servicio: no sabe si el PersonaDAO que recibe habla con Derby o con MySQL,
 * solo conoce la interfaz PersonaDAO.
 */
public class PersonaService {

    private final PersonaDAO dao;

    public PersonaService(PersonaDAO dao) {
        this.dao = dao;
    }

    public void agregarPersona(Persona persona) throws SQLException {
        dao.crearTablaSiNoExiste();
        dao.guardar(persona);
    }

    public List<Persona> obtenerTodasLasPersonas() throws SQLException {
        dao.crearTablaSiNoExiste();
        return dao.listarTodas();
    }
}
