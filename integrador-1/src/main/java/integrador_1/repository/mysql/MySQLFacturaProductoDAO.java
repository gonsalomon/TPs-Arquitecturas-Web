package integrador_1.repository.mysql;

import integrador_1.dao.FacturaProductoDAO;
import integrador_1.entity.FacturaProducto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLFacturaProductoDAO implements FacturaProductoDAO {
    private Connection connection;

    public MySQLFacturaProductoDAO(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS factura_producto(" +
                "idFactura int," +
                "idProducto int," +
                "cantidad int," +
                "primary key(idFactura,idProducto)," +
                "foreign key(idFactura) references factura(idFactura) ON DELETE CASCADE ON UPDATE CASCADE," +
                "foreign key(idProducto) references producto(idProducto) ON DELETE CASCADE ON UPDATE CASCADE)";
        try {
            Statement st = connection.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // "id" es idFactura (asi lo trata el resto de la interfaz, ver comentario de updateProducto)
    @Override
    public FacturaProducto findById(Long id) {
        String sql = "SELECT * FROM factura_producto WHERE idFactura=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FacturaProducto> findAll() {
        List<FacturaProducto> resultado = new ArrayList<>();
        String sql = "SELECT * FROM factura_producto";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                resultado.add(map(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<List<FacturaProducto>> findByProducto(Long idProducto) {
        List<FacturaProducto> resultado = new ArrayList<>();
        String sql = "SELECT * FROM factura_producto WHERE idProducto=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, idProducto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resultado.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado);
    }

    @Override
    public void create(FacturaProducto fp) {
        String sql = "INSERT INTO factura_producto(idFactura,idProducto,cantidad) VALUES(?,?,?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, fp.getIdFactura());
            ps.setInt(2, fp.getIdProducto());
            ps.setInt(3, fp.getCantidad());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(FacturaProducto fp) {
        String sql = "UPDATE factura_producto SET cantidad=? WHERE idFactura=? AND idProducto=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, fp.getCantidad());
            ps.setInt(2, fp.getIdFactura());
            ps.setInt(3, fp.getIdProducto());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // la FK (idProducto) se puede reasignar sin tocar la PK (idFactura)
    @Override
    public void updateProducto(Long id, Long idProducto) {
        String sql = "UPDATE factura_producto SET idProducto=? WHERE idFactura=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, idProducto);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM factura_producto WHERE idFactura=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM factura_producto";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private FacturaProducto map(ResultSet rs) throws SQLException {
        return new FacturaProducto(
                rs.getInt("idFactura"),
                rs.getInt("idProducto"),
                rs.getInt("cantidad")
        );
    }
}
