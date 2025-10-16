package mx.desarollo.integration;

import mx.desarollo.facade.FacadeUsuario;

public class ServiceFacadeLocator {


    private static FacadeUsuario facadeUsuario;



    public static FacadeUsuario getInstanceFacadeUsuario() {
        if (facadeUsuario == null) {
            facadeUsuario = new FacadeUsuario();
            return facadeUsuario;
        } else {
            return facadeUsuario;
        }
    }
}
