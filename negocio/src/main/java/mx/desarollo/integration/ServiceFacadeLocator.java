package mx.desarollo.integration;

import mx.desarollo.facade.FacadeArticulo;
import mx.desarollo.facade.FacadeLogin;

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
}