package helper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mx.desarollo.entity.Articulo;
import mx.desarollo.facade.FacadeCarrito;

// Clase helper que gestiona el estado y operaciones del carrito de compras
public class Carrito implements Serializable {
    private static final long serialVersionUID = 1L;

    // Lista interna que mantiene los ítems añadidos al carrito
    private final List<CarritoItem> items = new ArrayList<>();

    // Fachada para validaciones de negocio (ej. stock)
    private final FacadeCarrito facadeCarrito = new FacadeCarrito();

    // Retorna la lista actual de ítems
    public List<CarritoItem> getItems() {
        return items;
    }

    // Verifica si el carrito no tiene ítems
    public boolean isVacio() {
        return items.isEmpty();
    }

    // Método auxiliar para acceso tipo propiedad JSF
    public boolean getVacio() {
        return isVacio();
    }

    // Cuenta la cantidad total de productos (suma de cantidades de cada ítem)
    public int getConteoTotalItems() {
        return items.stream().mapToInt(CarritoItem::getCantidad).sum();
    }

    // Agrega un artículo: incrementa cantidad si ya existe, o lo crea si es nuevo. Valida stock.
    public boolean agregarArticulo(Articulo articulo) {
        if (articulo == null) return false;

        // Busca si el artículo ya está en el carrito
        Optional<CarritoItem> existente = items.stream()
                .filter(i -> i.getArticulo() != null
                        && i.getArticulo().getIdarticulo() != null
                        && i.getArticulo().getIdarticulo().equals(articulo.getIdarticulo()))
                .findFirst();

        if (existente.isPresent()) {
            CarritoItem it = existente.get();
            int nuevaCantidad = it.getCantidad() + 1;

            // Valida stock antes de incrementar
            if (!facadeCarrito.verificarStock(articulo, nuevaCantidad)) {
                return false;
            }
            it.setCantidad(nuevaCantidad);
        } else {
            // Valida stock para el primer ítem
            if (!facadeCarrito.verificarStock(articulo, 1)) {
                return false;
            }
            items.add(new CarritoItem(articulo, 1));
        }
        return true;
    }

    // Elimina un ítem específico del carrito
    public void eliminarItem(CarritoItem item) {
        if (item == null) return;
        items.removeIf(i -> i.equals(item));
    }

    // Incrementa en 1 la cantidad de un ítem si hay stock disponible
    public void incrementarCantidad(CarritoItem item) {
        if (item == null) return;
        for (CarritoItem i : items) {
            if (i.equals(item)) {
                int nuevaCant = i.getCantidad() + 1;
                if (facadeCarrito.verificarStock(i.getArticulo(), nuevaCant)) {
                    i.setCantidad(nuevaCant);
                }
                break;
            }
        }
    }

    // Decrementa en 1 la cantidad; elimina el ítem si llega a cero
    public void decrementarCantidad(CarritoItem item) {
        if (item == null) return;
        for (CarritoItem i : new ArrayList<>(items)) {
            if (i.equals(item)) {
                int nueva = i.getCantidad() - 1;
                if (nueva <= 0) {
                    items.remove(i);
                } else {
                    i.setCantidad(nueva);
                }
                break;
            }
        }
    }

    // Limpia todos los ítems del carrito
    public void vaciar() {
        items.clear();
    }

    // Calcula la suma total de los precios de todos los ítems
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CarritoItem::getPrecioTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public BigDecimal getTotal() {
        return getSubtotal();
    }
}