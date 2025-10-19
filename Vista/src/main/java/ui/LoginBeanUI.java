package ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import helper.LoginHelper;

import java.io.IOException;
import java.io.Serializable;

@Named("loginUI")
@SessionScoped
public class LoginBeanUI implements Serializable {

    private LoginHelper loginHelper;
    private String correo;
    private String contrasena;
    private Object usuario; // can be Administrador or Empleado

    public LoginBeanUI() {
        loginHelper = new LoginHelper();
    }

    public void login() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        if (correo == null || correo.trim().isEmpty() ||
                contrasena == null || contrasena.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Debe llenar ambos campos", "Ingrese su correo y contraseña"));
            return;
        }

        Object result = loginHelper.login(correo, contrasena);

        if (result instanceof mx.desarollo.entity.Administrador) {
            usuario = result;
            context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/index.xhtml");
        } else if (result instanceof mx.desarollo.entity.Empleado) {
            usuario = result;
            context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/login.xhtml");
        } else {
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN, "Usuario o contraseña incorrecta", "Intente de nuevo"));
        }
    }

    public void logout() throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/AutenticacionUsuario.xhtml");
    }

    public void preventBackAfterLogout() throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        var externalContext = facesContext.getExternalContext();
        var response = (jakarta.servlet.http.HttpServletResponse) externalContext.getResponse();

        // Disable browser and proxy caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        boolean notLoggedIn = false;

        // ✅ If there's no session or user, mark as not logged in
        if (externalContext.getSession(false) == null || usuario == null) {
            notLoggedIn = true;
        }

        // ✅ If user exists but has no correo, check based on type
        else if (usuario instanceof mx.desarollo.entity.Administrador admin) {
            notLoggedIn = (admin.getCorreo() == null || admin.getCorreo().isBlank());
        } else if (usuario instanceof mx.desarollo.entity.Empleado emp) {
            notLoggedIn = (emp.getCorreo() == null || emp.getCorreo().isBlank());
        } else {
            notLoggedIn = true;
        }

        // ✅ Redirect if not logged in
        if (notLoggedIn) {
            externalContext.redirect(externalContext.getRequestContextPath() + "/AutenticacionUsuario.xhtml");
        }
    }
    public void registrarAdministrador() {
        try {
            mx.desarollo.entity.Administrador admin = new mx.desarollo.entity.Administrador();
            admin.setCorreo(correo);
            admin.setContrasena(contrasena);
            loginHelper.saveAdministrador(admin);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Administrador registrado exitosamente", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al registrar administrador", e.getMessage()));
        }
    }

    public void registrarEmpleado() {
        try {
            mx.desarollo.entity.Empleado emp = new mx.desarollo.entity.Empleado();
            emp.setCorreo(correo);
            emp.setContrasena(contrasena);
            loginHelper.saveEmpleado(emp);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Empleado registrado exitosamente", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al registrar empleado", e.getMessage()));
        }
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Object getUsuario() { return usuario; }
}