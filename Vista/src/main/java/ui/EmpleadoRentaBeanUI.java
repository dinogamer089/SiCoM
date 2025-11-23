package ui;

import helper.RentaHelper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import mx.desarollo.entity.Renta;
import mx.desarollo.entity.Empleado;
import java.io.Serializable;
import java.util.List;

@Named("empleadoRentaUI")
@ViewScoped
public class EmpleadoRentaBeanUI implements Serializable {

    private RentaHelper rentaHelper;
    private List<Renta> rentasPendientes;

    @Inject
    private LoginBeanUI loginBean;

    private Empleado empleadoLogueado;
    private Renta rentaSeleccionada;
    private Integer idRentaSeleccionada;
    private String siguienteEstado;

    /**
     * Metodo de inicializacion PostConstruct.
     * Instancia el helper y valida la seguridad de la sesion verificando si existe un empleado logueado.
     * @Throws No lanza excepciones, pero advierte en consola si no hay sesion valida.
     */
    @PostConstruct
    public void init() {
        rentaHelper = new RentaHelper();

        // Validación de Seguridad y Sesiin
        if (loginBean != null && loginBean.getUsuario() instanceof Empleado) {
            this.empleadoLogueado = (Empleado) loginBean.getUsuario();
            cargarRentas();
        } else {
            // Si no hay sesión entonces redirigir (opcional, o mostrar lista vacía)
            System.out.println("ADVERTENCIA: No hay empleado en sesión.");
        }
    }

    /**
     * Metodo para cargar la lista de rentas filtrada para la vista del empleado.
     * Invoca al helper para obtener rentas disponibles y las ya asignadas al empleado actual.
     */
    public void cargarRentas() {
        if (this.empleadoLogueado != null) {
            this.rentasPendientes = rentaHelper.obtenerRentasDisponiblesYAsignadas(this.empleadoLogueado.getId());
        }
    }

    /**
     * Metodo para cargar una renta especifica basada en el ID seleccionado.
     * Ademas calcula y establece el siguiente estado logico posible para dicha renta.
     */
    public void cargarRentaSeleccionada() {
        if (idRentaSeleccionada != null) {
            this.rentaSeleccionada = rentaHelper.findById(idRentaSeleccionada);
            if(this.rentaSeleccionada != null) {
                this.siguienteEstado = rentaHelper.calcularSiguienteEstado(rentaSeleccionada.getEstado());
            }
        }
    }

    /**
     * Metodo transaccional para avanzar el estado de la renta y gestionar la autoasignacion.
     * Actualiza el registro del empleado, nombres de entrega/recoleccion y ejecuta el cambio de estado.
     * @Throws Captura Exception general para manejar errores de logica o base de datos y notificar al usuario via FacesMessage.
     */
    public void avanzarEstado() {
        if (rentaSeleccionada != null && siguienteEstado != null) {
            try {
                if ("En reparto".equals(siguienteEstado) || "En recoleccion".equals(siguienteEstado)) {
                    rentaSeleccionada.setIdEmpleado(this.empleadoLogueado);
                }

                if ("Entregado".equals(siguienteEstado)) {
                    rentaSeleccionada.setEntregado(this.empleadoLogueado.getNombre());
                } else if ("Finalizada".equals(siguienteEstado)) {
                    rentaSeleccionada.setRecogido(this.empleadoLogueado.getNombre());
                    rentaSeleccionada.setIdEmpleado(null);
                }

                rentaSeleccionada.setEstado(siguienteEstado);

                rentaHelper.actualizarRenta(rentaSeleccionada);
                rentaHelper.cambiarEstado(rentaSeleccionada.getId(), siguienteEstado);

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Estado actualizado a: " + siguienteEstado));

                cargarRentaSeleccionada();

            } catch (Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo procesar la solicitud."));
            }
        }
    }

    /**
     * Metodo de navegacion para redirigir a la vista de detalle de una renta.
     * @Params Objeto de tipo Renta renta
     * @return Un String con la regla de navegacion y el parametro idRenta incluido.
     */
    public String irADetalle(Renta renta) {
        return "DetalleRentaEmpleado.xhtml?idRenta=" + renta.getId() + "&faces-redirect=true";
    }

    // Getters y Setters
    public List<Renta> getRentasPendientes() { return rentasPendientes; }
    public Renta getRentaSeleccionada() { return rentaSeleccionada; }
    public Integer getIdRentaSeleccionada() { return idRentaSeleccionada; }
    public void setIdRentaSeleccionada(Integer idRentaSeleccionada) { this.idRentaSeleccionada = idRentaSeleccionada; }
    public String getSiguienteEstado() { return siguienteEstado; }
}