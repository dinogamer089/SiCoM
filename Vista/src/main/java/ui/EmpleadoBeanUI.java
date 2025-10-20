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
public class EmpleadoBeanUI implements Serializable {

    private List<Empleado> empleados;
    private Empleado empleadoSeleccionado;
    private Empleado nuevoEmpleado;
    private String nuevaContrasena;

    // ========== FLAG PARA COMUNICAR ÉXITO A JAVASCRIPT ==========
    private boolean operacionExitosa = false;
    private FacadeEmpleado facadeEmpleado;

    @PostConstruct
    public void init() {
        facadeEmpleado = ServiceFacadeLocator.getInstanceFacadeEmpleado();
        cargarEmpleados();
        this.nuevoEmpleado = new Empleado();
    }

    private void cargarEmpleados() {
        this.empleados = facadeEmpleado.getAllEmpleados();
    }

    public void prepararNuevoEmpleado() {
        this.nuevoEmpleado = new Empleado();
        this.operacionExitosa = false;
    }

    public void prepararModalReset() {
        this.nuevaContrasena = null;
        this.operacionExitosa = false;
    }

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

            // Guardar
            facadeEmpleado.saveEmpleado(nuevoEmpleado);
            // Recargar lista y limpiar formulario
            cargarEmpleados();
            this.nuevoEmpleado = new Empleado();

            this.operacionExitosa = true;
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Empleado guardado correctamente."));
        } catch (Exception e) {
            this.operacionExitosa = false;
            String mensajeError = "No se pudo guardar el empleado.";
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                mensajeError = "El correo electrónico ya está registrado.";
            } else if (e.getMessage() != null) {
                mensajeError += " Causa: " + e.getMessage();
            }

            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", mensajeError));
            ctx.validationFailed();
            System.err.println("Error al guardar empleado: " + e.getMessage());
            e.printStackTrace();
        }


        PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
    }

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
            ctx.addMessage("formResetPass:newPass", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La contraseña no puede estar vacía."));
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
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Contraseña actualizada correctamente."));
        } catch (Exception e) {
            this.operacionExitosa = false;
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error Fatal", "No se pudo actualizar la contraseña: " + e.getMessage()));
            ctx.validationFailed();
        }


        PrimeFaces.current().ajax().addCallbackParam("operacionExitosa", this.operacionExitosa);
    }


    public void eliminarEmpleado() {
        if (empleadoSeleccionado != null) {
            try {
                facadeEmpleado.deleteEmpleado(empleadoSeleccionado);
                cargarEmpleados();
                empleadoSeleccionado = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Empleado eliminado."));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "No se pudo eliminar el empleado: " + e.getMessage()));
            }
        }
    }

    // ========== GETTERS Y SETTERS ==========
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
    public boolean isOperacionExitosa() { return operacionExitosa;
    }
    public void setOperacionExitosa(boolean operacionExitosa) { this.operacionExitosa = operacionExitosa;
    }
}