package mx.desarollo.delegate;

import mx.desarollo.entity.Articulo;
import mx.avanti.desarollo.integration.ServiceLocator;

import java.util.List;

public class DelegateArticulo {

    public List<Articulo> findAllArticulos(){
        return ServiceLocator.getInstanceArticuloDAO().findAll();
    }
}
