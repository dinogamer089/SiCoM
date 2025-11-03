package mx.desarollo.integration;

import mx.desarollo.facade.*;

public class ServiceFacadeLocator {
    private static FacadeLogin facadeLogin;

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

    private static FacadeCliente facadeCliente;

    public static FacadeCliente getInstanceFacadeCliente() {
        if (facadeCliente == null) {
            facadeCliente = new FacadeCliente();
        }
        return facadeCliente;
    }

    private static FacadeRenta facadeRenta;

    public static FacadeRenta getInstanceFacadeRenta() {
        if (facadeRenta == null) {
            facadeRenta = new FacadeRenta();
        }
        return facadeRenta;
    }

    private static FacadeDetalle facadeDetalle;

    public static FacadeDetalle getInstanceFacadeDetalle() {
        if (facadeDetalle == null) {
            facadeDetalle = new FacadeDetalle();
        }
        return facadeDetalle;
    }
}