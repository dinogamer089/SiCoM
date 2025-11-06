package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Renta;

import java.util.List;

public class DelegateRenta {
    public List<Renta> findAllCotizaciones(){
        return ServiceLocator.getInstanceRentaDAO().obtenerTodosCotizaciones();
    }

    public Renta findRentaId(Integer idRenta){
        return ServiceLocator.getInstanceRentaDAO().obtenerRentaID(idRenta);
    }
}
