package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;

import java.util.List;

public class DelegateArticulo {

    public List<Articulo> listarCatalogoCliente() {
        return ServiceLocator.getInstanceArticuloDAO().listarActivosConStock();
    }

    public List<Articulo> findAllArticulos() {
        return ServiceLocator.getInstanceArticuloDAO().obtenerTodos();
    }

    public void saveArticuloWithImage(Articulo articulo, Imagen imagen) {
        ServiceLocator.getInstanceArticuloDAO().saveWithImage(articulo, imagen);
    }
}
