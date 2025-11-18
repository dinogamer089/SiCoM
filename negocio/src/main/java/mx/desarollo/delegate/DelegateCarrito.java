package mx.desarollo.delegate;

import mx.desarollo.entity.Articulo;

public class DelegateCarrito {


    public boolean hayStock(Articulo articulo, int cantidadSolicitada) {
        if (articulo == null) return false;
        Integer stock = articulo.getUnidades();
        if (stock == null) return true;
        return stock >= cantidadSolicitada;
    }
}



