package mx.desarollo.integration;

import mx.desarollo.facade.FacadeLogin;
import mx.desarollo.facade.FacadeArticulo;

public class ServiceFacadeLocator {
    private static FacadeLogin facadeLogin;
    private static FacadeArticulo facadeArticulo;

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
}