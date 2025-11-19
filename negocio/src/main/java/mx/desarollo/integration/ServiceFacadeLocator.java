package mx.desarollo.integration;

import mx.desarollo.facade.FacadeArticulo;
import mx.desarollo.facade.FacadeLogin;
import mx.desarollo.facade.FacadeCombinacionMesa;

public class ServiceFacadeLocator {
    private static FacadeLogin facadeLogin;
    private static FacadeArticulo facadeArticulo;
    private static FacadeCombinacionMesa facadeCombinacionMesa;

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

    public static FacadeCombinacionMesa getInstanceFacadeCombinacionMesa() {
        if (facadeCombinacionMesa == null) {
            facadeCombinacionMesa = new FacadeCombinacionMesa();
        }
        return facadeCombinacionMesa;
    }
}
