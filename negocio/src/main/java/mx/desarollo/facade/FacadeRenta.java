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

    public java.util.List<Renta> obtenerRentas(){
        return delegateRenta.findAllRentas();
    }

    public Renta obtenerRentaId(Integer idRenta){
        return delegateRenta.findRentaId(idRenta);
    }

    public void cambiarEstado(Integer idRenta, String nuevoEstado){
        delegateRenta.cambiarEstado(idRenta, nuevoEstado);
    }
}
