package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateLogin;
import mx.desarollo.entity.Administrador;
import mx.desarollo.entity.Empleado;

public class FacadeLogin {

    private final DelegateLogin delegateLogin;

    public FacadeLogin() {
        this.delegateLogin = new DelegateLogin();
    }

    public Object login(String password, String correo) {
        return delegateLogin.login(password, correo);
    }
    public void saveAdministrador(Administrador admin) {
        delegateLogin.saveAdministrador(admin);
    }

    public void saveEmpleado(Empleado empleado) {
        delegateLogin.saveEmpleado(empleado);
    }
}