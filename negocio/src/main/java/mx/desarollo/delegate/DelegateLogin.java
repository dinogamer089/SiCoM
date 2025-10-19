package mx.desarollo.delegate;

import mx.avanti.desarollo.dao.AdministradorDAO;
import mx.avanti.desarollo.dao.EmpleadoDAO;
import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Administrador;
import mx.desarollo.entity.Empleado;
import org.mindrot.jbcrypt.BCrypt;

public class DelegateLogin {

    private final AdministradorDAO administradorDAO;
    private final EmpleadoDAO empleadoDAO;

    public DelegateLogin() {
        this.administradorDAO = ServiceLocator.getInstanceAdministradorDAO();
        this.empleadoDAO = ServiceLocator.getInstanceEmpleadoDAO();
    }

    public Object login(String password, String correo) {
        // Try to find in Administrador
        Administrador admin = administradorDAO.findByCorreo(correo);
        if (admin != null && BCrypt.checkpw(password, admin.getContrasena())) {
            return admin;
        }

        // Try to find in Empleado
        Empleado empleado = empleadoDAO.findByCorreo(correo);
        if (empleado != null && BCrypt.checkpw(password, empleado.getContrasena())) {
            return empleado;
        }

        // Not found
        return null;
    }

    public void saveAdministrador(Administrador admin) {
        // Encrypt password before saving
        String hashed = BCrypt.hashpw(admin.getContrasena(), BCrypt.gensalt());
        admin.setContrasena(hashed);
        administradorDAO.save(admin);
    }

    public void saveEmpleado(Empleado empleado) {
        // Encrypt password before saving
        String hashed = BCrypt.hashpw(empleado.getContrasena(), BCrypt.gensalt());
        empleado.setContrasena(hashed);
        empleadoDAO.save(empleado);
    }
}