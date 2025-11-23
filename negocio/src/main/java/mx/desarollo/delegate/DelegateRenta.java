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

    public void cambiarEstado(Integer idRenta, String nuevoEstado){
        ServiceLocator.getInstanceRentaDAO().cambiarEstadoRenta(idRenta, nuevoEstado);
    }

    public List<Renta> findAllRentas(){
        return ServiceLocator.getInstanceRentaDAO().obtenerTodosRentas();
    }

    public void actualizarRenta(Renta renta){
        ServiceLocator.getInstanceRentaDAO().update(renta);
    }

    /**
     * Metodo para obtener las rentas disponibles o asignadas a un empleado especifico.
     * Llama a la instancia de RentaDAO a traves del ServiceLocator para realizar la consulta.
     * @Throws Si la base de datos rechaza la peticion de busqueda o hay un error en la capa DAO.
     * @Params Objeto de tipo Integer idEmpleado
     * @return Una lista con las rentas encontradas disponibles o asignadas al empleado.
     */
    public List<Renta> obtenerRentasDisponiblesYAsignadas(Integer idEmpleado){
        return ServiceLocator.getInstanceRentaDAO().obtenerRentasDisponiblesYAsignadas(idEmpleado);
    }
}