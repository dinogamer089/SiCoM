package mx.desarollo.integration;

import mx.desarollo.facade.FacadeUsuario;
import mx.desarollo.facade.FacadeArticulo;

public class ServiceFacadeLocator {

    private static FacadeArticulo facadeArticulo;
    private static FacadeUsuario facadeUsuario;

    public static FacadeArticulo getInstanceFacadeArticulo() {
        if (facadeArticulo == null) {
            facadeArticulo = new FacadeArticulo();
            return facadeArticulo;
        } else {
            return facadeArticulo;
        }
    }

    public static FacadeUsuario getInstanceFacadeUsuario() {
        if (facadeUsuario == null) {
            facadeUsuario = new FacadeUsuario();
            return facadeUsuario;
        } else {
            return facadeUsuario;
        }
    }
}
