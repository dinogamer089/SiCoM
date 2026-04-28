package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Articulo;

import java.time.LocalDate;

public class DelegateCarrito {

    public boolean hayStock(Articulo articulo, int cantidadSolicitada) {
        if (articulo == null) return false;
        Integer stock = articulo.getUnidades();
        if (stock == null) return true;
        return stock >= cantidadSolicitada;
    }

    public boolean hayStockEnFecha(Articulo articulo, int cantidadSolicitada, LocalDate fecha) {
        if (articulo == null) return false;
        Integer stock = articulo.getUnidades();
        if (stock == null) return true;
        if (fecha == null) return stock >= cantidadSolicitada;

        int reservado = ServiceLocator.getInstanceStockReservadoDAO()
                .obtenerReservado(articulo.getId(), fecha);
        int disponible = Math.max(stock - reservado, 0);
        return cantidadSolicitada <= disponible;
    }

    public int obtenerDisponibleEnFecha(Articulo articulo, LocalDate fecha) {
        if (articulo == null) return 0;
        Integer stock = articulo.getUnidades();
        if (stock == null) return 0;
        if (fecha == null) return stock;
        int reservado = ServiceLocator.getInstanceStockReservadoDAO()
                .obtenerReservado(articulo.getId(), fecha);
        return Math.max(stock - reservado, 0);
    }

    /**
     * Verifica disponibilidad considerando el dia mas saturado del rango.
     * Usado cuando una renta abarca varios dias.
     */
    public boolean hayStockEnRango(Articulo articulo, int cantidadSolicitada, LocalDate fechaInicio, LocalDate fechaFin) {
        if (articulo == null) return false;
        Integer stock = articulo.getUnidades();
        if (stock == null) return true;
        if (fechaInicio == null && fechaFin == null) return stock >= cantidadSolicitada;
        if (fechaInicio == null) fechaInicio = fechaFin;
        if (fechaFin == null) fechaFin = fechaInicio;

        int maxReservado = ServiceLocator.getInstanceStockReservadoDAO()
                .obtenerMaximoReservadoEnRango(articulo.getId(), fechaInicio, fechaFin);
        int disponible = Math.max(stock - maxReservado, 0);
        return cantidadSolicitada <= disponible;
    }

    /**
     * Devuelve la cantidad disponible MINIMA en cualquier dia del rango (cuello de botella).
     */
    public int obtenerDisponibleEnRango(Articulo articulo, LocalDate fechaInicio, LocalDate fechaFin) {
        if (articulo == null) return 0;
        Integer stock = articulo.getUnidades();
        if (stock == null) return 0;
        if (fechaInicio == null && fechaFin == null) return stock;
        if (fechaInicio == null) fechaInicio = fechaFin;
        if (fechaFin == null) fechaFin = fechaInicio;

        int maxReservado = ServiceLocator.getInstanceStockReservadoDAO()
                .obtenerMaximoReservadoEnRango(articulo.getId(), fechaInicio, fechaFin);
        return Math.max(stock - maxReservado, 0);
    }
}



