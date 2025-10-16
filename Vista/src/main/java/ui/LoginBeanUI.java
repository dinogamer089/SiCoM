package ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import helper.LoginHelper;
import mx.desarollo.entity.Rol;
import mx.desarollo.entity.Usuario;

import java.io.IOException;
import java.io.Serializable;

@Named("loginUI")
@SessionScoped
public class LoginBeanUI implements Serializable {

    private LoginHelper loginHelper;
    private Usuario usuario;

    public LoginBeanUI() {
        loginHelper = new LoginHelper();
    }

    @PostConstruct
    public void init() {
        usuario = new Usuario();
    }

    public void login() throws IOException {
        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty() ||
                usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Debe llenar ambos campos",
                    "Ingrese su correo y contraseña"));
            return;
        }

        Usuario us = loginHelper.Login(usuario.getCorreo(), usuario.getContrasena());
        if (us != null && us.getId() != null && us.getRol().equals(Rol.Administrador)) {
            usuario = us;
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/index.xhtml");
        } else if(us != null && us.getId() != null && us.getRol().equals(Rol.Empleado)){
            usuario = us;
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/login.xhtml");
        }else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Usuario o contraseña incorrecta", "Intente de nuevo"));
        }
    }
    public void logout() throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().invalidateSession(); // Destroys the session

        // Redirect to the login page (adjust path if needed)
        facesContext.getExternalContext().redirect(
                facesContext.getExternalContext().getRequestContextPath() + "/AutenticacionUsuario.xhtml"
        );
    }
    public void preventBackAfterLogout() throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        var externalContext = facesContext.getExternalContext();
        var response = (jakarta.servlet.http.HttpServletResponse) externalContext.getResponse();

        // Disable browser and proxy caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        if (externalContext.getSession(false) == null ||
                usuario == null || usuario.getCorreo() == null) {
            externalContext.redirect(externalContext.getRequestContextPath() + "/AutenticacionUsuario.xhtml");
        }
    }

    public Usuario getUsuario() { return usuario; }
    public void register(){LoginHelper.guardarUsuario(usuario);}
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}