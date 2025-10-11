package mx.desarollo.delegate;

import mx.desarollo.entity.Alumno;
import mx.avanti.desarollo.integration.ServiceLocator;

public class DelegateAlumno {
    public void saveAlumno(Alumno alumno){
        ServiceLocator.getInstanceAlumnoDAO().save(alumno);
    }

}