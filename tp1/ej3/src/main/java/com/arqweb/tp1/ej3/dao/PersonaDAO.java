package com.arqweb.tp1.ej3.dao;

import com.arqweb.tp1.ej3.model.Persona;

import java.sql.SQLException;
import java.util.List;

/**
 * Abstrae qué motor de base de datos se usa (Derby, MySQL, el que sea).
 * Quien use un PersonaDAO no necesita saber si atrás hay JDBC contra Derby o contra MySQL.
 */
public interface PersonaDAO {

    void crearTablaSiNoExiste() throws SQLException;

    void guardar(Persona persona) throws SQLException;

    List<Persona> listarTodas() throws SQLException;
}
