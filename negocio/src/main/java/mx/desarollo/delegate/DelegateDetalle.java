package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Detallerenta;

import java.util.List;

public class DelegateDetalle {
    public List<Detallerenta> findAllDetalles(){
        return ServiceLocator.getInstanceDetalleDAO().findAll();
    }

    public void saveDetalle(Detallerenta detalle){
        ServiceLocator.getInstanceDetalleDAO().save(detalle);}
}
