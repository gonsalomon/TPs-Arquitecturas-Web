package integrador_1.dto;

/* Punto 4: cliente + total que se le facturo.
 * Mismo criterio que TopProductoRecaudador: el total es un calculo, no una
 * columna de cliente, asi que no ensucia la entity.
 */
public record ClienteFacturacion(int idCliente, String nombre, String email, double totalFacturado) {

    @Override
    public String toString() {
        return String.format("#%-3d %-25s %-35s total facturado: $%,.2f",
                idCliente, nombre, email, totalFacturado);
    }
}
