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

    // Fecha de fin (recoleccion) - se mantiene como "fechaSeleccionada" por compatibilidad
    private LocalDate fechaSeleccionada;

    // Fecha de inicio (entrega) - puede ser igual a fechaSeleccionada para rentas de un solo dia
    private LocalDate fechaInicio;

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
            // Si existe, validamos si hay stock para incrementar (en el rango)
            CarritoItem it = existente.get();
            int nuevaCantidad = it.getCantidad() + 1;
            if (!facadeCarrito.verificarStockEnRango(articulo, nuevaCantidad, getRangoInicio(), fechaSeleccionada)) {
                return false; // No hay suficiente stock
            }
            it.setCantidad(nuevaCantidad);
        } else {
            // Si es nuevo, validamos stock para 1 unidad en el rango
            if (!facadeCarrito.verificarStockEnRango(articulo, 1, getRangoInicio(), fechaSeleccionada)) {
                return false; // No hay stock
            }
            items.add(new CarritoItem(articulo, 1));
        }
        return true;
    }

    private LocalDate getRangoInicio() {
        return (fechaInicio != null) ? fechaInicio : fechaSeleccionada;
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
                // Validación de stock antes del cambio (en el rango completo)
                if (facadeCarrito.verificarStockEnRango(i.getArticulo(), nuevaCant, getRangoInicio(), fechaSeleccionada)) {
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
            // validar contra stock en el rango de fechas
            if (facadeCarrito.verificarStockEnRango(i.getArticulo(), nuevaCantidad, getRangoInicio(), fechaSeleccionada)) {
                i.setCantidad(nuevaCantidad);
                i.setAjustadoPorStock(false);
                i.setAvisoAjuste(null);
            } else {
                int disponible = facadeCarrito.obtenerDisponibleEnRango(i.getArticulo(), getRangoInicio(), fechaSeleccionada);
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

    public LocalDate getFechaInicio() { return fechaInicio; }

    // Método auxiliar para consultar cuánto stock real hay en la fecha (compatibilidad)
    public int checkStock(Articulo articulo, LocalDate fecha) {
        return facadeCarrito.obtenerDisponibleEnFecha(articulo, fecha);
    }

    // Consulta disponibilidad en el rango actual del carrito
    public int checkStockEnRango(Articulo articulo) {
        return facadeCarrito.obtenerDisponibleEnRango(articulo, getRangoInicio(), fechaSeleccionada);
    }

    /**
     * Actualiza la fecha del carrito y revalida todos los ítems.
     * Compatibilidad: usa la misma fecha como inicio y fin (renta de un solo dia).
     */
    public List<String> actualizarFechaSeleccionada(LocalDate nuevaFecha) {
        return actualizarRangoFechas(nuevaFecha, nuevaFecha);
    }

    /**
     * Actualiza el rango de fechas del carrito y revalida todos los items.
     * Si al cambiar el rango, un articulo ya no tiene stock suficiente, se ajusta o elimina.
     * @return Lista de mensajes avisando al usuario de los ajustes.
     */
    public List<String> actualizarRangoFechas(LocalDate nuevaFechaInicio, LocalDate nuevaFechaFin) {
        this.fechaInicio = nuevaFechaInicio;
        this.fechaSeleccionada = nuevaFechaFin;
        List<String> mensajes = new ArrayList<>();
        if (nuevaFechaFin == null) return mensajes;

        List<CarritoItem> snapshot = new ArrayList<>(items);
        for (CarritoItem it : snapshot) {
            Articulo art = it.getArticulo();
            if (art == null || art.getId() == null) continue;

            int disponible = facadeCarrito.obtenerDisponibleEnRango(
                    art,
                    (nuevaFechaInicio != null ? nuevaFechaInicio : nuevaFechaFin),
                    nuevaFechaFin
            );

            if (it.getCantidad() > disponible) {
                if (disponible <= 0) {
                    items.remove(it);
                    mensajes.add("Se eliminó '" + safeNombre(art) + "' por no haber stock en el rango seleccionado.");
                } else {
                    it.setCantidad(disponible);
                    mensajes.add("Se redujo '" + safeNombre(art) + "' a " + disponible + " por disponibilidad en el rango.");
                }
            }
        }
        return mensajes;
    }

    private String safeNombre(Articulo a) {
        try { return a.getNombre(); } catch (Exception e) { return "articulo"; }
    }
}
