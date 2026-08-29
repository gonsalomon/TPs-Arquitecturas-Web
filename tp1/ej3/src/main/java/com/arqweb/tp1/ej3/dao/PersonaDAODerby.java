package com.arqweb.tp1.ej3.dao;

import com.arqweb.tp1.ej3.model.Persona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAODerby implements PersonaDAO {

    private static final String URL = "jdbc:derby:personaDB;create=true";

    @Override
    public void crearTablaSiNoExiste() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE Persona (id INT PRIMARY KEY, nombre VARCHAR(100), edad INT)");
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) throw e; // ya existía
        }
    }

    @Override
    public void guardar(Persona p) throws SQLException {
        String sql = "INSERT INTO Persona (id, nombre, edad) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getEdad());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (!"23505".equals(e.getSQLState())) throw e; // ya insertada
        }
    }

    @Override
    public List<Persona> listarTodas() throws SQLException {
        List<Persona> resultado = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Persona ORDER BY id")) {
            while (rs.next()) {
                resultado.add(new Persona(rs.getInt("id"), rs.getString("nombre"), rs.getInt("edad")));
            }
        }
        return resultado;
    }
}
