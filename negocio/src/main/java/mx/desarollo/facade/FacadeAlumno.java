package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateAlumno;
import mx.desarollo.entity.Alumno;

public class FacadeAlumno {

    private final DelegateAlumno delegateAlumno;

    public FacadeAlumno() {
        this.delegateAlumno = new DelegateAlumno();
    }

    public void guardarAlumno(Alumno alumno){
        delegateAlumno.saveAlumno(alumno);
    }

}
