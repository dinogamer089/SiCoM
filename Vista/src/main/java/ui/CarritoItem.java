package ui;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * Item dentro del carrito en sesion
 */
public class CarritoItem implements Serializable {
    private Integer articuloId;
    private String nombre;
    private BigDecimal precioUnitario;
    private int cantidad = 1;

    public CarritoItem(Integer articuloId, String nombre, BigDecimal precioUnitario) {
        this.articuloId = articuloId;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
    }

    public Integer getArticuloId() { return articuloId; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
