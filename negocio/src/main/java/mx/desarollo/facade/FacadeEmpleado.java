package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateEmpleado;
import mx.desarollo.entity.Empleado;
import java.util.List;

public class FacadeEmpleado {

    private final DelegateEmpleado delegateEmpleado;

    public FacadeEmpleado() {
        this.delegateEmpleado = new DelegateEmpleado();
    }

    public List<Empleado> getAllEmpleados() {
        return delegateEmpleado.getAllEmpleados();
    }

    public void saveEmpleado(Empleado empleado) {
        delegateEmpleado.saveEmpleado(empleado);
    }

    public void updateEmpleado(Empleado empleado) {
        delegateEmpleado.updateEmpleado(empleado);
    }

    public void deleteEmpleado(Empleado empleado) {
        delegateEmpleado.deleteEmpleado(empleado);
    }
}