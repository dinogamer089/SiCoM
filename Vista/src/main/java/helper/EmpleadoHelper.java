package helper;

import mx.desarollo.entity.Empleado;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.io.Serializable;
import java.util.List;

public class EmpleadoHelper implements Serializable {

    /**
     * Llama al facade para obtener todos los empleados.
     */
    public List<Empleado> getAllEmpleados() {
        return ServiceFacadeLocator.getInstanceFacadeEmpleado().getAllEmpleados();
    }

    /**
     * Llama al facade para guardar un nuevo empleado.
     */
    public void guardarEmpleado(Empleado empleado) {
        ServiceFacadeLocator.getInstanceFacadeEmpleado().saveEmpleado(empleado);
    }

    /**
     * Llama al facade para actualizar un empleado.
     */
    public void actualizarEmpleado(Empleado empleado) {
        ServiceFacadeLocator.getInstanceFacadeEmpleado().updateEmpleado(empleado);
    }

}