package ui;

import helper.RentaHelper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import mx.desarollo.entity.Comentario;
import mx.desarollo.entity.Renta;
import mx.desarollo.entity.Empleado;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.ArrayList;
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

    // Variables para el sistema de comentarios
    private String comentarioEntrega;
    private String comentarioRecoleccion;
    private String tituloDialogoComentario;
    private String nuevoComentarioTexto;
    private String estadoPendienteDeGuardar;

    /**
     * Método de inicialización PostConstruct.
     * Instancia el helper y valida la seguridad de la sesión verificando si existe un empleado logueado.
     * @Throws No lanza excepciones, pero advierte en consola si no hay sesión válida.
     */
    @PostConstruct
    public void init() {
        rentaHelper = new RentaHelper();

        // Validación de Seguridad y Sesión
        if (loginBean != null && loginBean.getUsuario() instanceof Empleado) {
            this.empleadoLogueado = (Empleado) loginBean.getUsuario();
            cargarRentas();
        } else {
            // Si no hay sesión entonces redirigir (opcional, o mostrar lista vacía)
            System.out.println("ADVERTENCIA: No hay empleado en sesión.");
        }
    }

    /**
     * Método para cargar la lista de rentas filtrada para la vista del empleado.
     * Invoca al helper para obtener rentas disponibles y las ya asignadas al empleado actual.
     */
    public void cargarRentas() {
        if (this.empleadoLogueado != null) {
            this.rentasPendientes = rentaHelper.obtenerRentasDisponiblesYAsignadas(this.empleadoLogueado.getId());
        }
    }

    /**
     * Método para cargar una renta específica basada en el ID seleccionado.
     * Además calcula y establece el siguiente estado lógico posible para dicha renta.
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
     * Método que verifica si el siguiente estado requiere un comentario.
     * Si es "Entregado" o "Finalizada", muestra el diálogo de comentarios.
     */
    public void verificarSiRequiereComentario() {
        if (siguienteEstado != null) {
            this.estadoPendienteDeGuardar = siguienteEstado;

            if ("Entregado".equals(siguienteEstado) || "Finalizada".equals(siguienteEstado)) {
                this.nuevoComentarioTexto = ""; // Limpiar texto anterior

                if ("Entregado".equals(siguienteEstado)) {
                    this.tituloDialogoComentario = "Detalles en la entrega";
                } else {
                    this.tituloDialogoComentario = "Detalles en la recolección";
                }

                PrimeFaces.current().ajax().update("dlgComentarioEstado");
                PrimeFaces.current().executeScript("PF('dlgComentarioEstado').show();");
            } else {
                // Si no requiere comentario, avanzar directamente
                avanzarEstado();
            }
        }
    }

    /**
     * Método para confirmar el cambio de estado con o sin comentario.
     * Guarda el comentario si existe y luego actualiza el estado.
     */
    public void confirmarCambioConComentario() {
        // Si hay texto, crea y guarda el comentario
        if (nuevoComentarioTexto != null && !nuevoComentarioTexto.trim().isEmpty()) {
            Comentario c = new Comentario();
            c.setComentario(nuevoComentarioTexto);
            c.setIdRenta(rentaSeleccionada);

            // Define el tipo según el estado
            if ("Entregado".equals(this.estadoPendienteDeGuardar)) {
                c.setTipo("Entrega");
            } else {
                c.setTipo("Recoleccion");
            }

            rentaHelper.guardarComentario(c);
        }

        // Actualiza el estado de la renta
        avanzarEstado();
    }

    /**
     * Método transaccional para avanzar el estado de la renta y gestionar la autoasignación.
     * Actualiza el registro del empleado, nombres de entrega/recolección y ejecuta el cambio de estado.
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public boolean avanzarEstado() {
        if (rentaSeleccionada != null && siguienteEstado != null) {
            try {
                // Autoasignación del empleado
                if ("En reparto".equals(siguienteEstado) || "En recoleccion".equals(siguienteEstado)) {
                    rentaSeleccionada.setIdEmpleado(this.empleadoLogueado);
                }

                // Registrar nombre del empleado que completó la acción
                if ("Entregado".equals(siguienteEstado)) {
                    rentaSeleccionada.setEntregado(this.empleadoLogueado.getNombre());
                    rentaSeleccionada.setIdEmpleado(null); // Liberar asignación
                } else if ("Finalizada".equals(siguienteEstado)) {
                    rentaSeleccionada.setRecogido(this.empleadoLogueado.getNombre());
                    rentaSeleccionada.setIdEmpleado(null); // Liberar asignación
                }

                rentaSeleccionada.setEstado(siguienteEstado);

                // Actualizar en base de datos
                rentaHelper.actualizarRenta(rentaSeleccionada);
                rentaHelper.cambiarEstado(rentaSeleccionada.getId(), siguienteEstado);

                // Recargar la renta para actualizar la vista
                cargarRentaSeleccionada();

                return true;

            } catch (Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().validationFailed();
                return false;
            }
        }
        return false;
    }

    /**
     * Método para cargar los comentarios de entrega y recolección de una renta.
     * Utilizado cuando se presiona el botón "Ver detalles".
     */
    public void cargarComentariosRenta() {
        this.comentarioEntrega = "Sin comentarios de entrega.";
        this.comentarioRecoleccion = "Sin comentarios de recolección.";

        if (rentaSeleccionada != null) {
            List<Comentario> comentarios = rentaHelper.obtenerComentariosPorRenta(rentaSeleccionada.getId());

            for (Comentario c : comentarios) {
                if ("Entrega".equalsIgnoreCase(c.getTipo())) {
                    this.comentarioEntrega = c.getComentario();
                } else if ("Recoleccion".equalsIgnoreCase(c.getTipo())) {
                    this.comentarioRecoleccion = c.getComentario();
                }
            }
        }
    }

    /**
     * Método de navegación para redirigir a la vista de detalle de una renta.
     * @Params Objeto de tipo Renta renta
     * @return Un String con la regla de navegación y el parámetro idRenta incluido.
     */
    public String irADetalle(Renta renta) {
        return "DetalleRentaEmpleado.xhtml?idRenta=" + renta.getId() + "&faces-redirect=true";
    }

    // Getters y Setters
    public List<Renta> getRentasPendientes() {
        return rentasPendientes;
    }

    public Renta getRentaSeleccionada() {
        return rentaSeleccionada;
    }

    public Integer getIdRentaSeleccionada() {
        return idRentaSeleccionada;
    }

    public void setIdRentaSeleccionada(Integer idRentaSeleccionada) {
        this.idRentaSeleccionada = idRentaSeleccionada;
    }

    public String getSiguienteEstado() {
        return siguienteEstado;
    }

    public String getComentarioEntrega() {
        return comentarioEntrega;
    }

    public void setComentarioEntrega(String comentarioEntrega) {
        this.comentarioEntrega = comentarioEntrega;
    }

    public String getComentarioRecoleccion() {
        return comentarioRecoleccion;
    }

    public void setComentarioRecoleccion(String comentarioRecoleccion) {
        this.comentarioRecoleccion = comentarioRecoleccion;
    }

    public String getTituloDialogoComentario() {
        return tituloDialogoComentario;
    }

    public String getNuevoComentarioTexto() {
        return nuevoComentarioTexto;
    }

    public void setNuevoComentarioTexto(String nuevoComentarioTexto) {
        this.nuevoComentarioTexto = nuevoComentarioTexto;
    }
}