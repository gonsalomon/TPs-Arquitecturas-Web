package com.arqweb.tp1.ej1;

import java.sql.*;

/**
 * TP1 - Ejercicio 1
 * Configuración de Derby y consultas sobre la tabla Persona, usando JDBC "a mano"
 * (sin ningún patrón todavía, eso viene en el Ejercicio 3).
 */
public class DerbyPersonaApp {

    // create=true: si la base "personaDB" no existe, Derby la crea en este mismo directorio
    private static final String URL = "jdbc:derby:personaDB;create=true";

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            System.out.println("Conectado a Derby (embebido).");
            crearTabla(conn);
            insertarDatosDePrueba(conn);
            ejecutarConsultas(conn);
        }
    }

    private static void crearTabla(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE Persona (" +
                    "id INT PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL, " +
                    "edad INT)");
            System.out.println("Tabla Persona creada.");
        } catch (SQLException e) {
            // X0Y32 = "la tabla ya existe" en Derby. Lo ignoramos para poder re-ejecutar el programa.
            if (!"X0Y32".equals(e.getSQLState())) throw e;
            System.out.println("La tabla Persona ya existía, sigo.");
        }
    }

    private static void insertarDatosDePrueba(Connection conn) throws SQLException {
        String sql = "INSERT INTO Persona (id, nombre, edad) VALUES (?, ?, ?)";
        Object[][] datos = {
                {1, "Ana", 28},
                {2, "Bruno", 34},
                {3, "Carla", 22},
                {4, "Diego", 41}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] fila : datos) {
                try {
                    ps.setInt(1, (int) fila[0]);
                    ps.setString(2, (String) fila[1]);
                    ps.setInt(3, (int) fila[2]);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    // 23505 = violación de clave primaria (ya insertada en una corrida anterior)
                    if (!"23505".equals(e.getSQLState())) throw e;
                }
            }
        }
    }

    private static void ejecutarConsultas(Connection conn) throws SQLException {
        System.out.println("=== Todas las personas ===");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Persona ORDER BY id")) {
            while (rs.next()) {
                System.out.printf("%d - %s (%d años)%n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getInt("edad"));
            }
        }

        System.out.println("=== Mayores de 30 (ordenadas por edad desc) ===");
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT nombre, edad FROM Persona WHERE edad > ? ORDER BY edad DESC")) {
            ps.setInt(1, 30);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("%s (%d años)%n", rs.getString("nombre"), rs.getInt("edad"));
                }
            }
        }

        System.out.println("=== Total de personas cargadas ===");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Persona")) {
            if (rs.next()) System.out.println("Total: " + rs.getInt("total"));
        }

        System.out.println("=== Actualización: cumpleaños de Ana (id=1) ===");
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Persona SET edad = edad + 1 WHERE id = ?")) {
            ps.setInt(1, 1);
            int filas = ps.executeUpdate();
            System.out.println("Filas actualizadas: " + filas);
        }
    }
}
