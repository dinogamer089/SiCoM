package ui;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrito en memoria (sesion)
 * NO se muestra en esta US, solo recibe "agregar"Ca
 */
public class Carrito implements Serializable {

    private final Map<Integer, CarritoItem> items = new LinkedHashMap<>();

    public void agregar(Integer id, String nombre, BigDecimal precio) {
        items.compute(id, (k, v) -> {
            if (v == null) return new CarritoItem(id, nombre, precio);
            v.setCantidad(v.getCantidad() + 1);
            return v;
        });
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public BigDecimal getTotal() {
        return items.values().stream()
                .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void limpiar() {
        items.clear();
    }
}
