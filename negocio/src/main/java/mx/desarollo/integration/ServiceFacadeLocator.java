package mx.desarollo.integration;

import mx.desarollo.facade.FacadeLogin;

public class ServiceFacadeLocator {
    private static FacadeLogin facadeLogin;

    public static FacadeLogin getInstanceFacadeLogin() {
        if (facadeLogin == null) {
            facadeLogin = new FacadeLogin();
        }
        return facadeLogin;
    }
}