package helper;

import java.io.Serial;
import java.io.Serializable;
import mx.desarollo.entity.Articulo;

import java.math.BigDecimal;
import java.util.Objects;

// Clase auxiliar que representa una línea de detalle en el carrito (artículo + cantidad)
public class CarritoItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Artículo asociado y la cantidad seleccionada por el usuario
    private Articulo articulo;
    private int cantidad;

    // Constructores por defecto y parametrizado
    public CarritoItem() {}

    public CarritoItem(Articulo articulo, int cantidad) {
        this.articulo = articulo;
        this.cantidad = cantidad;
    }

    // Getters y setters estándar
    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    // Setter con validación para asegurar que la cantidad no sea negativa
    public void setCantidad(int cantidad) {
        this.cantidad = Math.max(0, cantidad);
    }

    // Calcula el subtotal de este ítem (precio del artículo * cantidad)
    public BigDecimal getPrecioTotal() {
        if (articulo == null || articulo.getPrecio() == null) return BigDecimal.ZERO;
        return articulo.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }

    // Sobrescritura de equals y hashCode para identificar ítems basados en el ID del artículo
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CarritoItem that = (CarritoItem) o;

        if (this.articulo == null || that.articulo == null) return false;
        return Objects.equals(this.articulo.getIdarticulo(), that.articulo.getIdarticulo());
    }

    @Override
    public int hashCode() {
        return articulo != null && articulo.getIdarticulo() != null ?
                articulo.getIdarticulo().hashCode() : 0;
    }
}