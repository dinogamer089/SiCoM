package mx.desarollo.integration;

import mx.desarollo.facade.FacadeArticulo;
import mx.desarollo.facade.FacadeLogin;

public class ServiceFacadeLocator {
    private static FacadeLogin facadeLogin;
    private static FacadeArticulo facadeArticulo;

    public static FacadeLogin getInstanceFacadeLogin() {
        if (facadeLogin == null) {
            facadeLogin = new FacadeLogin();
        }
        return facadeLogin;
    }

    private static FacadeArticulo facadeArticulo;

    public static FacadeArticulo getInstanceFacadeArticulo() {
        if (facadeArticulo == null) {
            facadeArticulo = new FacadeArticulo();
        }
        return facadeArticulo;
    }

    private static FacadeEmpleado facadeEmpleado;

    public static FacadeEmpleado getInstanceFacadeEmpleado() {
        if (facadeEmpleado == null) {
            facadeEmpleado = new FacadeEmpleado();
        }
        return facadeEmpleado;
    }

    private static FacadeRenta facadeRenta;

    public static FacadeRenta getInstanceFacadeRenta() {
        if (facadeRenta == null) {
            facadeRenta = new FacadeRenta();
        }
        return facadeRenta;
    }
}