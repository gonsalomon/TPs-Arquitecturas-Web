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

public class MySQLFacturaDAO implements FacturaDAO{
    private Connection connection;

    public MySQLFacturaDAO(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists(){
        String sql="CREATE TABLE IF NOT EXISTS factura(" +
                "idFactura int," +
                "idCliente int," +
                "primary key(idFactura)," +
                "foreign key(idCliente) references cliente(id) ON DELETE CASCADE ON UPDATE CASCADE)";
        try{
            Statement st=connection.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Factura findById(int id) {
        String sql="SELECT * FROM factura WHERE idFactura=?";
        try {
            PreparedStatement ps=connection.prepareStatement(sql);
            ps.setInt(1,id);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                return new Factura(
                        rs.getInt("idFactura"),
                        rs.getInt("idCliente")
                );
            }
            return null;
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Factura> findAll() {
        ArrayList<Factura> facturas=new ArrayList<>();
        String sql="SELECT * FROM factura";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Factura factura = new Factura(
                        rs.getInt("idFactura"),
                        rs.getInt("idCliente")
                );
                facturas.add(factura);
            }
            return facturas;
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public void create(Factura factura) {
        String sql="INSERT INTO factura(idFactura,idCliente) VALUES(?,?)";
        try{
            PreparedStatement ps=connection.prepareStatement(sql);
            ps.setInt(1,factura.getIdFactura());
            ps.setInt(2,factura.getIdCliente());
            ps.executeUpdate();
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Factura factura) {
        String sql="UPDATE factura SET idCliente=? WHERE idFactura=?";
        try{
            PreparedStatement ps=connection.prepareStatement(sql);
            ps.setInt(1,factura.getIdCliente());
            ps.setInt(2,factura.getIdFactura());
            ps.executeUpdate();
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String sql="DELETE FROM factura WHERE idFactura=?";
        try{
            PreparedStatement ps=connection.prepareStatement(sql);
            ps.setInt(1,id);
            ps.executeUpdate();
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        String sql="DELETE FROM factura";
        try{
            PreparedStatement ps=connection.prepareStatement(sql);
            ps.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
