package mx.desarollo.integration;

import mx.desarollo.facade.FacadeUsuario;
import mx.desarollo.facade.FacadeEmpleado;

public class ServiceFacadeLocator {
    private static FacadeUsuario facadeUsuario;
    private static FacadeEmpleado facadeEmpleado;

    public static FacadeUsuario getInstanceFacadeUsuario() {
        if (facadeUsuario == null) {
            facadeUsuario = new FacadeUsuario();
            return facadeUsuario;
        } else {
            return facadeUsuario;
        }
    }

    public static FacadeEmpleado getInstanceFacadeEmpleado() {
        if (facadeEmpleado == null) {
            facadeEmpleado = new FacadeEmpleado();
            return facadeEmpleado;
        } else {
            return facadeEmpleado;
        }
    }
}