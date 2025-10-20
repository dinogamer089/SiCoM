package mx.desarollo.integration;

import mx.desarollo.facade.FacadeUsuario;
import mx.desarollo.facade.FacadeArticulo;

public class ServiceFacadeLocator {
    private static FacadeUsuario facadeUsuario;
    private static FacadeArticulo facadeArticulo;

    public static FacadeUsuario getInstanceFacadeUsuario() {
        if (facadeUsuario == null) {
            facadeUsuario = new FacadeUsuario();
            return facadeUsuario;
        } else {
            return facadeUsuario;
        }
    }

    public static FacadeArticulo getInstanceFacadeArticulo() {
        if (facadeArticulo == null) {
            facadeArticulo = new FacadeArticulo();
            return facadeArticulo;
        } else {
            return facadeArticulo;
        }
    }
}
