package mx.desarollo.facade;

import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;
import mx.desarollo.delegate.DelegateArticulo;

import java.util.List;

public class FacadeArticulo {
    private final DelegateArticulo delegateArticulo;

    public FacadeArticulo() {
        this.delegateArticulo = new DelegateArticulo();
    }

    public List<Articulo> obtenerArticulos(){
        return delegateArticulo.findAllArticulos();
    }

    public void crearArticuloConImagen(Articulo articulo, Imagen imagen) {
        delegateArticulo.saveArticuloWithImage(articulo, imagen);
    }
}