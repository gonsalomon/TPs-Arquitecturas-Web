package integrador_1.repository.mysql;

import integrador_1.dao.ProductoDAO;
import integrador_1.dto.TopProductoRecaudador;
import integrador_1.entity.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySQLProductoDAO implements ProductoDAO {
    private final Connection connection;

    public MySQLProductoDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Producto findById(int id) {
        String sql = "SELECT * FROM producto WHERE idProducto=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Producto> findAll() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                productos.add(map(rs));
            }
            return productos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(Producto p) {
        String sql = "INSERT INTO producto(idProducto,nombre,valor) VALUES(?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, p.getIdProducto());
            ps.setString(2, p.getNombre());
            ps.setDouble(3, p.getValor());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Producto p) {
        String sql = "UPDATE producto SET nombre=?, valor=? WHERE idProducto=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getValor());
            ps.setInt(3, p.getIdProducto());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM producto WHERE idProducto=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM producto");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Ej 3: producto con mayor recaudacion (cantidad vendida * valor) segun lo
     * que quedo registrado en factura_producto. El GROUP BY va por la PK de
     * producto, asi que nombre y valor son dependientes funcionales y MySQL 8
     * los acepta aun con ONLY_FULL_GROUP_BY activo.
     */
    @Override
    public TopProductoRecaudador findProductMaxFacturacion() {
        String sql = "SELECT p.idProducto, p.nombre, SUM(fp.cantidad * p.valor) AS totalRecaudado " +
                "FROM factura_producto fp " +
                "JOIN producto p ON fp.idProducto = p.idProducto " +
                "GROUP BY p.idProducto " +
                "ORDER BY totalRecaudado DESC " +
                "LIMIT 1";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return new TopProductoRecaudador(
                        rs.getInt("idProducto"),
                        rs.getString("nombre"),
                        rs.getDouble("totalRecaudado")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Producto map(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("idProducto"),
                rs.getString("nombre"),
                rs.getDouble("valor")
        );
    }
}
