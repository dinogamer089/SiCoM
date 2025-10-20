package mx.desarollo.delegate;

import mx.desarollo.entity.Articulo;
import mx.avanti.desarollo.integration.ServiceLocator;

import java.util.List;

public class DelegateArticulo {

    public List<Articulo> findAllArticulos(){
        return ServiceLocator.getInstanceArticuloDAO().findAll();
    }

    public void saveArticulo(Articulo articulo){
    ServiceLocator.getInstanceArticuloDAO().save(articulo);}

    public void deleteArticulo(Integer id) {
        try {

            Articulo articulo = ServiceLocator.getInstanceArticuloDAO()
                    .find(id)
                    .orElseThrow(() -> new RuntimeException("Artículo no encontrado con ID: " + id));

            ServiceLocator.getInstanceArticuloDAO().delete(articulo);
        } catch (Exception e) {
            System.err.println("Error en deleteArticulo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
