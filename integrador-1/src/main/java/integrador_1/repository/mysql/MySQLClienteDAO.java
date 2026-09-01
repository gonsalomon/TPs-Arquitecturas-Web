package integrador_1.repository.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

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
                "idCliente BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(100) NOT NULL," +
                "email VARCHAR(120) NOT NULL UNIQUE," +
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
                if(keys.next()) cl.setIdCliente(keys.getLong(1));
            }
        } catch (SQLException e){
            throw new RuntimeException("Error en create", e);
        }
    }

    //TODO 0 read n
    @Override
    public List<Cliente> findAll(){
        return null;
    }

    //TODO 1 read 1
    @Override
    public Cliente findById(Long id){
        return null;
    }

    @Override
    public void update(Cliente cl) {
        final String sql = "UPDATE cliente SET nombre = ?, email = ? WHERE idCliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cl.getNombre());
            ps.setString(2, cl.getEmail());
            ps.setLong(3, cl.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en update", e);
        }
    }

    @Override
    public void delete(Long id){
        final String sql = "DELETE FROM cliente WHERE idCliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
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

    @Override
    public Optional<List<Cliente>> sortClientesByFacturacion(){
        //TODO 2 copypaste de la query SQL desde un commit viejo
        return null;
    }

    //para qué chori nos hacen usar este map privado? ni idea
    private Cliente map(ResultSet rs) throws SQLException{
        Cliente cl = new Cliente();
        cl.setIdCliente(rs.getLong("idCliente"));
        cl.setNombre(rs.getString("nombre"));
        cl.setEmail(rs.getString("email"));

        return cl;
    }
}
