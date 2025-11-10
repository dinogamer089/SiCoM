package mx.desarollo.integration;

import mx.desarollo.facade.FacadeArticulo;
import mx.desarollo.facade.FacadeEmpleado;
import mx.desarollo.facade.FacadeLogin;
import mx.desarollo.facade.FacadeRenta;

public class ServiceFacadeLocator {
    private static FacadeLogin facadeLogin;
    private static FacadeArticulo facadeArticulo;
    private static FacadeEmpleado facadeEmpleado;

    private static FacadeRenta facadeRenta;

    public static FacadeLogin getInstanceFacadeLogin() {
        if (facadeLogin == null) {
            facadeLogin = new FacadeLogin();
        }
        return facadeLogin;
    }

    public static FacadeArticulo getInstanceFacadeArticulo() {
        if (facadeArticulo == null) {
            facadeArticulo = new FacadeArticulo();
        }
        return facadeArticulo;
    }

    public static FacadeEmpleado getInstanceFacadeEmpleado() {
        return new FacadeEmpleado();
    }

    public static FacadeRenta getInstanceFacadeRenta() {
        if (facadeRenta == null) {
            facadeRenta = new FacadeRenta();
        }
        return facadeRenta;
    }
}
