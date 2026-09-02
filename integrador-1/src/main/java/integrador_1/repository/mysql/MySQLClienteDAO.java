package integrador_1.repository.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import integrador_1.dao.ClienteDAO;
import integrador_1.entity.Cliente;

public class MySQLClienteDAO implements ClienteDAO{
    private final Connection cn;

    public MySQLClienteDAO(Connection cn){
        this.cn = cn;
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        final String sql = "CREATE TABLE IF NOT EXISTS cliente (" +
                "idCliente INT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(100) NOT NULL," +
                "email VARCHAR(120) NOT NULL UNIQUE" +
                ")";
        try (Statement st = cn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tabla 'cliente'", e);
        }
    }


    @Override
    public void create(Cliente cl){
        final String sql = "INSERT INTO cliente (nombre, email) VALUES (?, ?)";
        try(PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, cl.getNombre());
            ps.setString(2, cl.getEmail());
            ps.executeUpdate();
            try(ResultSet keys = ps.getGeneratedKeys()){
                if(keys.next()) cl.setIdCliente(keys.getInt(1));
            }
        } catch (SQLException e){
            throw new RuntimeException("Error en create", e);
        }
    }

    @Override
    public List<Cliente> findAll(){
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
    public Cliente findById(int id){
        final String sql = "SELECT * FROM cliente WHERE idCliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
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
    public void delete(int id){
        final String sql = "DELETE FROM cliente WHERE idCliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en delete", e);
        }
    }

    @Override
    public void deleteAll(){
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM cliente");
            st.execute("ALTER TABLE cliente AUTO_INCREMENT = 1");
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'usuarios'", e);
        }
    }

    // clientes ordenados por total facturado (cantidad * valor, sumado sobre
    // todas sus facturas). Uso LEFT JOIN para que aparezcan tambien los
    // clientes sin facturas (con 0), no solo los que ya compraron algo.
    @Override
    public List<Cliente> sortClientesByFacturacion(){
        List<Cliente> resultado = new ArrayList<>();
        final String sql = "SELECT c.idCliente, c.nombre, c.email, " +
                "COALESCE(SUM(fp.cantidad * p.valor), 0) AS totalFacturado " +
                "FROM cliente c " +
                "LEFT JOIN factura f ON f.idCliente = c.idCliente " +
                "LEFT JOIN factura_producto fp ON fp.idFactura = f.idFactura " +
                "LEFT JOIN producto p ON p.idProducto = fp.idProducto " +
                "GROUP BY c.idCliente, c.nombre, c.email " +
                "ORDER BY totalFacturado DESC";
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en sortClientesByFacturacion", e);
        }
        return resultado;
    }

    private Cliente map(ResultSet rs) throws SQLException{
        Cliente cl = new Cliente();
        cl.setIdCliente(rs.getInt("idCliente"));
        cl.setNombre(rs.getString("nombre"));
        cl.setEmail(rs.getString("email"));

        return cl;
    }
}
