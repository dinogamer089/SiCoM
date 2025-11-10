package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateCarrito;
import mx.desarollo.entity.Articulo;

public class FacadeCarrito {
    private final DelegateCarrito delegate;

    public FacadeCarrito() {
        this.delegate = new DelegateCarrito();
    }

    public boolean verificarStock(Articulo articulo, int cantidadSolicitada) {
        return delegate.hayStock(articulo, cantidadSolicitada);
    }

}