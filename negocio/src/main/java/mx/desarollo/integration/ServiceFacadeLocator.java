package mx.desarollo.integration;

import mx.desarollo.facade.FacadeArticulo;

public class ServiceFacadeLocator {

    private static FacadeArticulo facadeArticulo;

    public static FacadeArticulo getInstanceFacadeArticulo() {
        if (facadeArticulo == null) {
            facadeArticulo = new FacadeArticulo();
            return facadeArticulo;
        } else {
            return facadeArticulo;
        }
    }
}

