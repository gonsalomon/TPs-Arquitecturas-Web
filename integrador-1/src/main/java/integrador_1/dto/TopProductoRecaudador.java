package integrador_1.dto;

/* Punto 3: producto + lo que recaudo (cantidad vendida * valor).
 * Es un DTO y no una entity porque "recaudacion" no es una columna de producto:
 * sale de agregar factura_producto.
 */
public record TopProductoRecaudador(int idProducto, String nombre, double recaudacion) {

    @Override
    public String toString() {
        return String.format("#%d %-30s recaudo: $%,.2f", idProducto, nombre, recaudacion);
    }
}
