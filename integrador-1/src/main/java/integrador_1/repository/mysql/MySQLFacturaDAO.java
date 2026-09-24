package integrador_1.repository.mysql;

import integrador_1.dao.FacturaDAO;
import integrador_1.entity.Factura;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySQLFacturaDAO implements FacturaDAO {
    private final Connection connection;

    public MySQLFacturaDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Factura findById(int id) {
        String sql = "SELECT * FROM factura WHERE idFactura=?";
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
    public List<Factura> findAll() {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT * FROM factura";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                facturas.add(map(rs));
            }
            return facturas;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(Factura factura) {
        String sql = "INSERT INTO factura(idFactura,idCliente) VALUES(?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, factura.getIdFactura());
            ps.setInt(2, factura.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Factura factura) {
        String sql = "UPDATE factura SET idCliente=? WHERE idFactura=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, factura.getIdCliente());
            ps.setInt(2, factura.getIdFactura());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM factura WHERE idFactura=?";
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
            st.executeUpdate("DELETE FROM factura");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Factura map(ResultSet rs) throws SQLException {
        return new Factura(
                rs.getInt("idFactura"),
                rs.getInt("idCliente")
        );
    }
}
