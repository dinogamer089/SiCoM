package helper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mx.desarollo.entity.Articulo;
import mx.desarollo.facade.FacadeCarrito;

/**
 * Clase que gestiona el estado del carrito de compras.
 * Maneja la lista de ítems, cálculos de totales y validación de stock
 * dependiente de una fecha seleccionada (lógica de renta/reservas).
 */
public class Carrito implements Serializable {
    private static final long serialVersionUID = 1L;

    // Lista que mantiene los artículos agregados al carrito
    private final List<CarritoItem> items = new ArrayList<>();

    // Fachada para consultar disponibilidad en base de datos
    private final FacadeCarrito facadeCarrito = new FacadeCarrito();

    // Fecha clave para verificar si hay stock disponible en ese día específico
    private LocalDate fechaSeleccionada;

    public List<CarritoItem> getItems() { return items; }

    // Métodos utilitarios para verificar si el carrito está vacío (útil para renderizado en JSF)
    public boolean isVacio() { return items.isEmpty(); }
    public boolean getVacio() { return isVacio(); }

    // Retorna la suma total de unidades de todos los artículos
    public int getConteoTotalItems() {
        return items.stream().mapToInt(CarritoItem::getCantidad).sum();
    }

    /**
     * Agrega un artículo al carrito.
     * 1. Si ya existe, intenta sumar +1 a la cantidad.
     * 2. Si no existe, intenta agregarlo con cantidad 1.
     * En ambos casos valida stock con 'fechaSeleccionada'.
     */
    public boolean agregarArticulo(Articulo articulo) {
        if (articulo == null) return false;

        // Busca si el artículo ya está en la lista
        Optional<CarritoItem> existente = items.stream()
                .filter(i -> i.getArticulo() != null
                        && i.getArticulo().getId() != null
                        && i.getArticulo().getId().equals(articulo.getId()))
                .findFirst();

        if (existente.isPresent()) {
            // Si existe, validamos si hay stock para incrementar
            CarritoItem it = existente.get();
            int nuevaCantidad = it.getCantidad() + 1;
            if (!facadeCarrito.verificarStock(articulo, nuevaCantidad, fechaSeleccionada)) {
                return false; // No hay suficiente stock
            }
            it.setCantidad(nuevaCantidad);
        } else {
            // Si es nuevo, validamos stock para 1 unidad
            if (!facadeCarrito.verificarStock(articulo, 1, fechaSeleccionada)) {
                return false; // No hay stock
            }
            items.add(new CarritoItem(articulo, 1));
        }
        return true;
    }

    // Elimina un ítem completamente del carrito
    public void eliminarItem(CarritoItem item) {
        if (item == null) return;
        items.removeIf(i -> i.equals(item));
    }

    // Incrementa en 1 la cantidad de un ítem existente (si hay stock)
    public void incrementarCantidad(CarritoItem item) {
        if (item == null) return;
        for (CarritoItem i : items) {
            if (i.equals(item)) {
                int nuevaCant = i.getCantidad() + 1;
                // Validación de stock antes del cambio
                if (facadeCarrito.verificarStock(i.getArticulo(), nuevaCant, fechaSeleccionada)) {
                    i.setCantidad(nuevaCant);
                }
                break;
            }
        }
    }

    // Decrementa en 1 la cantidad. Si llega a 0, elimina el ítem.
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

    // Cambia la cantidad manualmente respetando disponibilidad
    public void cambiarCantidad(CarritoItem item, int nuevaCantidad) {
        if (item == null) return;
        for (CarritoItem i : new ArrayList<>(items)) {
            if (!i.equals(item)) continue;
            if (nuevaCantidad <= 0) {
                items.remove(i);
                break;
            }
            // validar contra stock por fecha
            if (facadeCarrito.verificarStock(i.getArticulo(), nuevaCantidad, fechaSeleccionada)) {
                i.setCantidad(nuevaCantidad);
                i.setAjustadoPorStock(false);
                i.setAvisoAjuste(null);
            } else {
                int disponible = checkStock(i.getArticulo(), fechaSeleccionada);
                if (disponible <= 0) {
                    items.remove(i);
                } else {
                    i.setCantidad(disponible);
                    i.setAjustadoPorStock(true);
                    i.setAvisoAjuste("La cantidad de '" + safeNombre(i.getArticulo()) + "' se ajustó a " + disponible + " por disponibilidad.");
                }
            }
            break;
        }
    }

    // Limpia todo el carrito
    public void vaciar() { items.clear(); }

    // Calcula el costo total sumando los subtotales de cada ítem
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CarritoItem::getPrecioTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() { return getSubtotal(); }

    public LocalDate getFechaSeleccionada() { return fechaSeleccionada; }

    // Método auxiliar para consultar cuánto stock real hay en la fecha
    public int checkStock(Articulo articulo, LocalDate fecha) {
        return facadeCarrito.obtenerDisponibleEnFecha(articulo, fecha);
    }

    /**
     * Actualiza la fecha del carrito y revalida todos los ítems.
     * Si al cambiar la fecha, un artículo ya no tiene stock suficiente:
     * Se ajusta su cantidad a lo máximo disponible.
     * O se elimina si la disponibilidad es 0.
     * Retorna una lista de mensajes avisando al usuario de los cambios.
     */
    public List<String> actualizarFechaSeleccionada(LocalDate nuevaFecha) {
        this.fechaSeleccionada = nuevaFecha;
        List<String> mensajes = new ArrayList<>();
        if (nuevaFecha == null) return mensajes;

        List<CarritoItem> snapshot = new ArrayList<>(items);
        for (CarritoItem it : snapshot) {
            Articulo art = it.getArticulo();
            if (art == null || art.getId() == null) continue;

            // Consultamos disponibilidad en la NUEVA fecha
            int disponible = checkStock(art, nuevaFecha);

            // Si lo que tenemos en el carrito supera lo disponible, ajustamos
            if (it.getCantidad() > disponible) {
                if (disponible <= 0) {
                    items.remove(it);
                    mensajes.add("Se eliminó '" + safeNombre(art) + "' por no haber stock en la nueva fecha.");
                } else {
                    it.setCantidad(disponible);
                    mensajes.add("Se redujo '" + safeNombre(art) + "' a " + disponible + " por disponibilidad en la nueva fecha.");
                }
            }
        }
        return mensajes;
    }

    private String safeNombre(Articulo a) {
        try { return a.getNombre(); } catch (Exception e) { return "articulo"; }
    }
}
