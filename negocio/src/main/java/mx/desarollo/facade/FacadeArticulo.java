package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateArticulo;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;

import java.util.List;

public class FacadeArticulo {

    private final DelegateArticulo delegateArticulo;

    public FacadeArticulo() {
        this.delegateArticulo = new DelegateArticulo();
    }

    // Catalogo
    public List<Articulo> listarCatalogoCliente() {
        return delegateArticulo.listarCatalogoCliente();
    }

    // Alta
    public List<Articulo> obtenerArticulos() {
        return delegateArticulo.findAllArticulos();
    }

    // Alta
    public void crearArticuloConImagen(Articulo articulo, Imagen imagen) {
        delegateArticulo.saveArticuloWithImage(articulo, imagen);
    }

    // Eliminación
    public void eliminarArticuloPorId(Integer id) {
        delegateArticulo.deleteArticuloById(id);
    }
}
