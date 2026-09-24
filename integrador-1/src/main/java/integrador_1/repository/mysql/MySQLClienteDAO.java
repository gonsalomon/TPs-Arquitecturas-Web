package integrador_1.repository.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import integrador_1.dao.ClienteDAO;
import integrador_1.dto.ClienteFacturacion;
import integrador_1.entity.Cliente;

public class MySQLClienteDAO implements ClienteDAO {
    private final Connection cn;

    public MySQLClienteDAO(Connection cn) {
        this.cn = cn;
    }

    /* El idCliente lo trae el CSV y las facturas lo referencian, asi que se
     * inserta explicito: NO se delega en un AUTO_INCREMENT (si el archivo
     * viniera desordenado o con huecos, las FK apuntarian a otro cliente).
     */
    @Override
    public void create(Cliente cl) {
        final String sql = "INSERT INTO cliente (idCliente, nombre, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cl.getIdCliente());
            ps.setString(2, cl.getNombre());
            ps.setString(3, cl.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en create", e);
        }
    }

    @Override
    public List<Cliente> findAll() {
        List<Cliente> resultado = new ArrayList<>();
        final String sql = "SELECT * FROM cliente ORDER BY idCliente";
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findAll", e);
        }
        return resultado;
    }

    @Override
    public Cliente findById(int id) {
        final String sql = "SELECT * FROM cliente WHERE idCliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findById", e);
        }
    }

    @Override
    public void update(Cliente cl) {
        final String sql = "UPDATE cliente SET nombre = ?, email = ? WHERE idCliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cl.getNombre());
            ps.setString(2, cl.getEmail());
            ps.setInt(3, cl.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en update", e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM cliente WHERE idCliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en delete", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM cliente");
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'cliente'", e);
        }
    }

    /* Ej 4: clientes ordenados por total facturado (cantidad * valor, sumado
     * sobre todas sus facturas). LEFT JOIN para que aparezcan tambien los
     * clientes sin facturas, con total 0, y no solo los que ya compraron.
     */
    @Override
    public List<ClienteFacturacion> sortClientesByFacturacion() {
        List<ClienteFacturacion> resultado = new ArrayList<>();
        final String sql = "SELECT c.idCliente, c.nombre, c.email, " +
                "COALESCE(SUM(fp.cantidad * p.valor), 0) AS totalFacturado " +
                "FROM cliente c " +
                "LEFT JOIN factura f ON f.idCliente = c.idCliente " +
                "LEFT JOIN factura_producto fp ON fp.idFactura = f.idFactura " +
                "LEFT JOIN producto p ON p.idProducto = fp.idProducto " +
                "GROUP BY c.idCliente, c.nombre, c.email " +
                "ORDER BY totalFacturado DESC " +
                "LIMIT 5;";
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(new ClienteFacturacion(
                        rs.getInt("idCliente"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getDouble("totalFacturado")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en sortClientesByFacturacion", e);
        }
        return resultado;
    }

    private Cliente map(ResultSet rs) throws SQLException {
        Cliente cl = new Cliente();
        cl.setIdCliente(rs.getInt("idCliente"));
        cl.setNombre(rs.getString("nombre"));
        cl.setEmail(rs.getString("email"));

        return cl;
    }
}
