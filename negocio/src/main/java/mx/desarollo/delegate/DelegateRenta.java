package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Cliente;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

public class DelegateRenta {
    public List<Renta> findAllCotizaciones(){
        return ServiceLocator.getInstanceRentaDAO().obtenerTodosCotizaciones();
    }

    public Renta findRentaId(Integer idRenta){
        return ServiceLocator.getInstanceRentaDAO().obtenerRentaID(idRenta);
    }

    public void cambiarEstado(Integer idRenta, String nuevoEstado){
        ServiceLocator.getInstanceRentaDAO().cambiarEstadoRenta(idRenta, nuevoEstado);
    }

    public List<Renta> findAllRentas(){
        return ServiceLocator.getInstanceRentaDAO().obtenerTodosRentas();
    }

    public void actualizarRenta(Renta renta){
        ServiceLocator.getInstanceRentaDAO().update(renta);
    }

    // Delegación para registrar una renta/cotización desde el carrito
    public void registrarRenta(Cliente cliente,
                               List<Detallerenta> detalles,
                               LocalDate fecha,
                               LocalTime hora,
                               String estado) {
        ServiceLocator.getInstanceRentaDAO().registrarRenta(cliente, detalles, fecha, hora, estado);
    }
}
