package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateRenta;
import mx.desarollo.entity.Cliente;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    public void actualizarRenta(Renta renta){
        delegateRenta.actualizarRenta(renta);
    }

    // Método requerido por el flujo de CarritoBean para registrar una renta/cotización
    public void registrarRenta(Cliente cliente,
                               List<Detallerenta> detalles,
                               LocalDate fecha,
                               LocalTime hora,
                               String estado) {
        delegateRenta.registrarRenta(cliente, detalles, fecha, hora, estado);
    }

    /**
     * Metodo para obtener las rentas disponibles o asignadas a un empleado especifico.
     * Llama al metodo del delegateRenta para procesar la solicitud.
     * @Throws Si hay un error en la comunicacion con el delegate o en la recuperacion de datos.
     * @Params Objeto de tipo Integer idEmpleado
     * @return Una lista de objetos Renta disponibles o asignadas al empleado.
     */
    public java.util.List<Renta> obtenerRentasDisponiblesYAsignadas(Integer idEmpleado){
        return delegateRenta.obtenerRentasDisponiblesYAsignadas(idEmpleado);
    }

    public void actualizarRentaConStock(Renta renta, LocalDate fechaAnterior) throws Exception {
        delegateRenta.actualizarRentaConStock(renta, fechaAnterior);
    }
}
