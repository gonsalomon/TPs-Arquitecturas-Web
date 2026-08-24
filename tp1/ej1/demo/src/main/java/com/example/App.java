package com.example;
import java.sql.*;

public class App {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:derby:myDB;create=true";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            try {
                stmt.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(50))");
            } catch (SQLException e) {
                if (!e.getSQLState().equals("X0Y32")) throw e;
            }

            //stmt.execute("INSERT INTO users VALUES (1, 'Gonza')");
            stmt.execute("INSERT INTO users VALUES (2, 'Nulo')");

            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " - " + rs.getString("name"));
            }
        }
    }
}