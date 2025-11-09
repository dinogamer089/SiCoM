package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateRenta;
import mx.desarollo.entity.Renta;

public class FacadeRenta {
    private final DelegateRenta delegateRenta;

    public FacadeRenta() {
        this.delegateRenta = new DelegateRenta();
    }

    public java.util.List<Renta> obtenerCotizaciones(){
        return delegateRenta.findAllCotizaciones();
    }

    public Renta obtenerRentaId(Integer idRenta){
        return delegateRenta.findRentaId(idRenta);
    }

    public void actualizarRenta(Renta renta){
        delegateRenta.actualizarRenta(renta);
    }
}
