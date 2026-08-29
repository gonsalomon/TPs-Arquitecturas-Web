package com.arqweb.tp1.ej3.dao;

import com.arqweb.tp1.ej3.model.Persona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAOMySQL implements PersonaDAO {

    private static final String URL =
            "jdbc:mysql://localhost:3306/persona_db" +
            "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    @Override
    public void crearTablaSiNoExiste() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Persona (id INT PRIMARY KEY, nombre VARCHAR(100), edad INT)");
        }
    }

    @Override
    public void guardar(Persona p) throws SQLException {
        String sql = "INSERT IGNORE INTO Persona (id, nombre, edad) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getEdad());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Persona> listarTodas() throws SQLException {
        List<Persona> resultado = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Persona ORDER BY id")) {
            while (rs.next()) {
                resultado.add(new Persona(rs.getInt("id"), rs.getString("nombre"), rs.getInt("edad")));
            }
        }
        return resultado;
    }
}
