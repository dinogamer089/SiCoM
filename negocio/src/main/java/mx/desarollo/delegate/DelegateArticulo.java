package mx.desarollo.delegate;

import mx.avanti.desarollo.dao.ArticuloDAO;
import mx.desarollo.entity.Articulo;
import mx.avanti.desarollo.integration.ServiceLocator;

import java.util.List;

public class DelegateArticulo {

    public List<Articulo> findAllArticulos(){
        return ServiceLocator.getInstanceArticuloDAO().findAll();
    }

    public void saveArticulo(Articulo articulo){
    ServiceLocator.getInstanceArticuloDAO().save(articulo);}

    public void updateArticulo(Articulo art) {
        ArticuloDAO dao = ServiceLocator.getInstanceArticuloDAO();
        dao.update(art);
    }
}
