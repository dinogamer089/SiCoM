package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateDetalle;
import mx.desarollo.entity.Detallerenta;

public class FacadeDetalle {
    private final DelegateDetalle delegateDetalle;

    public FacadeDetalle() {
        this.delegateDetalle = new DelegateDetalle();
    }

    public java.util.List<Detallerenta> obtenerDetalles(){
        return delegateDetalle.findAllDetalles();
    }

    public void guardarDetalle(Detallerenta detalle){
        delegateDetalle.saveDetalle(detalle);
    }
}
