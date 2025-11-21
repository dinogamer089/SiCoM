package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateCarrito;
import mx.desarollo.entity.Articulo;
import java.time.LocalDate;

public class FacadeCarrito {
    private final DelegateCarrito delegate;

    public FacadeCarrito() {
        this.delegate = new DelegateCarrito();
    }

    public boolean verificarStock(Articulo articulo, int cantidadSolicitada) {
        return delegate.hayStock(articulo, cantidadSolicitada);
    }

    public boolean verificarStock(Articulo articulo, int cantidadSolicitada, LocalDate fecha) {
        return delegate.hayStockEnFecha(articulo, cantidadSolicitada, fecha);
    }

    public int obtenerDisponibleEnFecha(Articulo articulo, LocalDate fecha) {
        return delegate.obtenerDisponibleEnFecha(articulo, fecha);
    }

}
