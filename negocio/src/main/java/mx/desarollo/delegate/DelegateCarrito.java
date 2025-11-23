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
                .obtenerReservado(articulo.getIdarticulo(), fecha);
        int disponible = Math.max(stock - reservado, 0);
        return cantidadSolicitada <= disponible;
    }

    public int obtenerDisponibleEnFecha(Articulo articulo, LocalDate fecha) {
        if (articulo == null) return 0;
        Integer stock = articulo.getUnidades();
        if (stock == null) return 0;
        if (fecha == null) return stock;
        int reservado = ServiceLocator.getInstanceStockReservadoDAO()
                .obtenerReservado(articulo.getIdarticulo(), fecha);
        return Math.max(stock - reservado, 0);
    }
}



