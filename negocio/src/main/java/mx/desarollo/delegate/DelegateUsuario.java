package mx.desarollo.delegate;

import mx.desarollo.entity.Usuario;
import mx.avanti.desarollo.integration.ServiceLocator;

import java.util.List;

public class DelegateUsuario {
    public Usuario login(String password, String correo){
        Usuario usuario = new Usuario();
        List<Usuario> usuarios = ServiceLocator.getInstanceUsuarioDAO().findAll();

        for(Usuario us:usuarios){
            if(us.getContrasena().equalsIgnoreCase(password) && us.getCorreo().equalsIgnoreCase(correo)){
                usuario = us;
            }
        }
        return usuario;
    }

    public void saveUsario(Usuario usuario){
        ServiceLocator.getInstanceUsuarioDAO().save(usuario);
    }

}