package ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Empleado;
import mx.desarollo.facade.FacadeEmpleado;
import mx.desarollo.integration.ServiceFacadeLocator;
import org.mindrot.jbcrypt.BCrypt;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.List;

@Named("empleadoUI")
@ViewScoped
public class EmpleadoBeanUI implements Serializable
{

    private List<Empleado> empleados;
    private Empleado empleadoSeleccionado;
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

            // Hash de contraseña
            String hashGenerado = BCrypt.hashpw(nuevoEmpleado.getContrasena(), BCrypt.gensalt());
            nuevoEmpleado.setContrasena(hashGenerado);

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
            String hashGenerado = BCrypt.hashpw(nuevaContrasena, BCrypt.gensalt());
            empleadoSeleccionado.setContrasena(hashGenerado);

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

    // --- INICIO: eliminarEmpleado ---
    public void eliminarEmpleado() {
        if (empleadoSeleccionado != null) {
            try {
                facadeEmpleado.deleteEmpleado(empleadoSeleccionado);
                cargarEmpleados();
                empleadoSeleccionado = null;

            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "No se pudo eliminar el empleado: " + e.getMessage()));
                e.printStackTrace();
            }
        }
    }
    // --- FIN: eliminarEmpleado ---

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
    public Empleado getNuevoEmpleado() { return nuevoEmpleado;
    }
    public void setNuevoEmpleado(Empleado nuevoEmpleado) { this.nuevoEmpleado = nuevoEmpleado;
    }
    public String getNuevaContrasena() { return nuevaContrasena;
    }
    public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena;
    }
}