package ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Empleado;
import mx.desarollo.facade.FacadeEmpleado;
import mx.desarollo.integration.ServiceFacadeLocator;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.List;

@Named("empleadoUI")
@ViewScoped
public class EmpleadoBeanUI implements Serializable
{

    private List<Empleado> empleados;
    private Empleado empleadoSeleccionado;
    private Integer empleadoAEliminarId;
    private Empleado nuevoEmpleado;
    private String nuevaContrasena;

    private boolean operacionExitosa = false;
    private FacadeEmpleado facadeEmpleado;

    @PostConstruct
    public void init() {
        facadeEmpleado = ServiceFacadeLocator.getInstanceFacadeEmpleado();
        cargarEmpleados();
        this.nuevoEmpleado = new Empleado();
    }

    // --- INICIO: cargarEmpleados ---
    private void cargarEmpleados() {
        this.empleados = facadeEmpleado.getAllEmpleados();
    }
    // --- FIN: cargarEmpleados ---

    // --- INICIO: prepararNuevoEmpleado ---
    public void prepararNuevoEmpleado() {
        this.nuevoEmpleado = new Empleado();
        this.operacionExitosa = false;
    }
    // --- FIN: prepararNuevoEmpleado ---

    // --- INICIO: prepararModalReset ---
    public void prepararModalReset() {
        this.nuevaContrasena = null;
        this.operacionExitosa = false;
    }
    // --- FIN: prepararModalReset ---

    // --- INICIO: recargarEmpleados ---
    public void recargarEmpleados() {
        cargarEmpleados();
    }
    // --- FIN: recargarEmpleados ---

    // --- INICIO: guardarEmpleado ---
    public void guardarEmpleado() {
        this.operacionExitosa = false;
        FacesContext ctx = FacesContext.getCurrentInstance();

        try {
            if (nuevoEmpleado.getNombre() == null || nuevoEmpleado.getNombre().trim().isEmpty() ||
                    nuevoEmpleado.getApellidoPaterno() == null || nuevoEmpleado.getApellidoPaterno().trim().isEmpty() ||
                    nuevoEmpleado.getApellidoMaterno() == null || nuevoEmpleado.getApellidoMaterno().trim().isEmpty()) {


                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Nombre y apellidos son obligatorios."));
                ctx.validationFailed();
                PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
                return;
            }

            if (nuevoEmpleado.getCorreo() == null || nuevoEmpleado.getCorreo().trim().isEmpty()) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El correo es obligatorio."));
                ctx.validationFailed();
                PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
                return;
            }

            if (nuevoEmpleado.getContrasena() == null || nuevoEmpleado.getContrasena().trim().isEmpty()) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La contraseña es obligatoria."));
                ctx.validationFailed();
                PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
                return;
            }

            // --- EDICION: SE QUITÓ BCRYPT AQUÍ PARA EVITAR DOBLE ENCRIPTACIÓN ---

            facadeEmpleado.saveEmpleado(nuevoEmpleado);
            cargarEmpleados();
            this.nuevoEmpleado = new Empleado();

            this.operacionExitosa = true;

        } catch (Exception e) {
            this.operacionExitosa = false;

            String mensajeRaiz = obtenerMensajeRaiz(e);
            String mensajeUsuario;
            String detalleUsuario;

            if (mensajeRaiz != null && (
                    mensajeRaiz.toLowerCase().contains("duplicate") ||
                            mensajeRaiz.toLowerCase().contains("unique constraint")
            )) {
                mensajeUsuario = "Correo duplicado";
                detalleUsuario = "El correo electrónico ya se encuentra registrado.";

            } else {
                mensajeUsuario = "Error al guardar";
                detalleUsuario = "No se pudo guardar el empleado.";
            }

            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensajeUsuario, detalleUsuario));
            ctx.validationFailed();

            System.err.println("Error al guardar empleado (excepción completa):");
            e.printStackTrace();
        }

        PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
    }
    // --- FIN: guardarEmpleado ---

    // --- INICIO: restablecerContrasena ---
    public void restablecerContrasena() {
        this.operacionExitosa = false;
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (empleadoSeleccionado == null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", "No hay ningún empleado seleccionado."));
            ctx.validationFailed();
            PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
            return;
        }

        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            ctx.addMessage("modalResetForm:newPass", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La contraseña no puede estar vacía."));
            ctx.validationFailed();
            PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
            return;
        }

        try {
            // --- EDICIÓN: SE QUITÓ BCRYPT AQUÍ ---
            empleadoSeleccionado.setContrasena(nuevaContrasena);

            facadeEmpleado.updateEmpleado(empleadoSeleccionado);

            cargarEmpleados();
            this.nuevaContrasena = null;

            this.operacionExitosa = true;


        } catch (Exception e) {
            this.operacionExitosa = false;
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error Fatal", "No se pudo actualizar la contraseña: " + e.getMessage()));
            ctx.validationFailed();
            e.printStackTrace();
        }

        PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
    }
    // --- FIN: restablecerContrasena ---

    // --- INICIO: prepararEliminacion ---
    public void prepararEliminacion() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean permitirEliminar = true;

        if (empleadoSeleccionado == null || empleadoSeleccionado.getId() == null) {
            permitirEliminar = false;
        } else if (facadeEmpleado.tieneAsignacionesPendientes(empleadoSeleccionado.getId())) {
            permitirEliminar = false;
            PrimeFaces.current().executeScript("mostrarToastSimple('No se puede eliminar el empleado ya que tiene asignaciones pendientes.');");
            ctx.validationFailed();
        }

        PrimeFaces.current().ajax().addCallbackParam("permitirEliminar", permitirEliminar);
    }
    // --- FIN: prepararEliminacion ---

    // --- INICIO: eliminarEmpleado ---
    public void eliminarEmpleado() {
        if (empleadoSeleccionado != null) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            try {
                Integer empleadoId = empleadoSeleccionado.getId();
                if (empleadoId != null && facadeEmpleado.tieneAsignacionesPendientes(empleadoId)) {
                    PrimeFaces.current().executeScript("mostrarToastSimple('No se puede eliminar el empleado ya que tiene asignaciones pendientes.');");
                    ctx.validationFailed();
                    return;
                }

                facadeEmpleado.deleteEmpleado(empleadoSeleccionado);
                cargarEmpleados();
                empleadoSeleccionado = null;

            } catch (IllegalStateException ise) {
                PrimeFaces.current().executeScript("mostrarToastSimple('No se puede eliminar el empleado ya que tiene asignaciones pendientes.');");
                ctx.validationFailed();
            } catch (Exception e) {
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "No se pudo eliminar el empleado: " + e.getMessage()));
                ctx.validationFailed();
                e.printStackTrace();
            }
        }
    }
    // --- FIN: eliminarEmpleado ---

    // --- INICIO: eliminarEmpleadoPorId ---
    /**
     * EliminaciÃ³n parametrizada por ID para evitar dependencias del estado seleccionado.
     */
    public void eliminarEmpleadoPorId(Integer empleadoId) {
        if (empleadoId == null) {
            return;
        }
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            Empleado empleado = facadeEmpleado.findById(empleadoId);
            if (empleado == null) {
                return;
            }

            if (facadeEmpleado.tieneAsignacionesPendientes(empleadoId)) {
                PrimeFaces.current().executeScript("mostrarToastSimple('No se puede eliminar el empleado ya que tiene asignaciones pendientes.');");
                ctx.validationFailed();
                return;
            }

            facadeEmpleado.deleteEmpleado(empleado);
            cargarEmpleados();
            if (empleadoSeleccionado != null && empleadoSeleccionado.getId() != null
                    && empleadoSeleccionado.getId().equals(empleadoId)) {
                empleadoSeleccionado = null;
            }
        } catch (IllegalStateException ise) {
            PrimeFaces.current().executeScript("mostrarToastSimple('No se puede eliminar el empleado ya que tiene asignaciones pendientes.');");
            ctx.validationFailed();
        } catch (Exception e) {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudo eliminar el empleado: " + e.getMessage()));
            ctx.validationFailed();
            e.printStackTrace();
        }
    }
    // --- FIN: eliminarEmpleadoPorId ---

    // --- INICIO: eliminarEmpleadoPorId (desde request param) ---
    public void eliminarEmpleadoPorId() {
        String param = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("empleadoId");
        Integer id = null;
        try {
            if (param != null) {
                id = Integer.valueOf(param);
            }
        } catch (NumberFormatException ignored) { }
        eliminarEmpleadoPorId(id);
    }
    // --- FIN: eliminarEmpleadoPorId (desde request param) ---

    // --- INICIO: obtenerMensajeRaiz ---
    private String obtenerMensajeRaiz(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable causa = throwable;
        // Sigue buscando la causa más profunda
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa.getMessage();
    }
    // --- FIN: obtenerMensajeRaiz ---


    // --- GETTERS Y SETTERS ---
    public List<Empleado> getEmpleados() { return empleados;
    }
    public void setEmpleados(List<Empleado> empleados) { this.empleados = empleados;
    }
    public Empleado getEmpleadoSeleccionado() { return empleadoSeleccionado;
    }
    public void setEmpleadoSeleccionado(Empleado empleadoSeleccionado) { this.empleadoSeleccionado = empleadoSeleccionado;
    }
    public Integer getEmpleadoAEliminarId() { return empleadoAEliminarId; }
    public void setEmpleadoAEliminarId(Integer empleadoAEliminarId) { this.empleadoAEliminarId = empleadoAEliminarId; }
    public Empleado getNuevoEmpleado() { return nuevoEmpleado;
    }
    public void setNuevoEmpleado(Empleado nuevoEmpleado) { this.nuevoEmpleado = nuevoEmpleado;
    }
    public String getNuevaContrasena() { return nuevaContrasena;
    }
    public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena;
    }
}
