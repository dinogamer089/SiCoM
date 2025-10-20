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
        Usuario us = loginHelper.Login(usuario.getCorreo(), usuario.getContrasena());
        if (us != null && us.getId() != null && us.getRol().equals(Rol.Administrador)) {
            usuario = us;
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/principalAdministrador.xhtml");
            usuario = new Usuario(); // clear to prevent reusing hashed password
        } else if(us != null && us.getId() != null && us.getRol().equals(Rol.Empleado)){
            usuario = us;
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/login.xhtml");
            usuario = new Usuario(); // clear to prevent reusing hashed password
        }else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Usuario o contraseña incorrecta", "Intente de nuevo"));
        }
    }

    public Usuario getUsuario() { return usuario; }
    public void register(){LoginHelper.guardarUsuario(usuario);}
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}