package mx.desarollo.delegate;

import mx.avanti.desarollo.dao.UsuarioDAO;
import mx.desarollo.entity.Usuario;
import mx.avanti.desarollo.integration.ServiceLocator;
import org.mindrot.jbcrypt.BCrypt;

public class DelegateUsuario {

    private final UsuarioDAO usuarioDAO;

    public DelegateUsuario() {
        this.usuarioDAO = ServiceLocator.getInstanceUsuarioDAO();
    }

    // 🔹 LOGIN
    public Usuario login(String password, String correo) {
        Usuario usuario = usuarioDAO.findByCorreo(correo);
        if (usuario != null && BCrypt.checkpw(password, usuario.getContrasena())) {
            return usuario;
        }
        return null; // invalid credentials
    }

    public void saveUsario(Usuario usuario){
        String hashed = BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt(12));
        usuario.setContrasena(hashed);
        ServiceLocator.getInstanceUsuarioDAO().save(usuario);
    }

}