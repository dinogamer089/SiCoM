package mx.desarollo.integration;

import mx.desarollo.facade.FacadeAlumno;
import mx.desarollo.facade.FacadeUsuario;

public class ServiceFacadeLocator {

    private static FacadeAlumno facadeAlumno;
    private static FacadeUsuario facadeUsuario;

    public static FacadeAlumno getInstanceFacadeAlumno() {
        if (facadeAlumno == null) {
            facadeAlumno = new FacadeAlumno();
            return facadeAlumno;
        } else {
            return facadeAlumno;
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
