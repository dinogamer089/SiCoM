package helper;

import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.util.List;

public class ArticuloHelper {

    public List<Articulo> obtenerTodas() {
        return ServiceFacadeLocator.getInstanceFacadeArticulo().obtenerArticulos();
    }

    public void guardarConImagen(Articulo articulo, Imagen imagen) {
        ServiceFacadeLocator.getInstanceFacadeArticulo().crearArticuloConImagen(articulo, imagen);
    }
}