package mx.desarollo.facade;

import mx.desarollo.entity.Articulo;
import mx.desarollo.delegate.DelegateArticulo;

public class FacadeArticulo {
    private final DelegateArticulo delegateArticulo;

    public FacadeArticulo() {
        this.delegateArticulo = new DelegateArticulo();
    }

    public java.util.List<Articulo> obtenerArticulos(){
        return delegateArticulo.findAllArticulos();
    }

    public void guardarArticulo(Articulo articulo){
        delegateArticulo.saveArticulo(articulo);
    }
}