package helper;

import mx.desarollo.entity.Empleado;
import mx.desarollo.entity.Renta;
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

    /**
     * Llama al facade para eliminar un empleado.
     */
    public void deleteEmpleado(Empleado empleado) {
        ServiceFacadeLocator.getInstanceFacadeEmpleado().deleteEmpleado(empleado);
    }

    /**
     * Verifica si el empleado tiene entregas o recolecciones pendientes.
     */
    public boolean tieneAsignacionesPendientes(Integer empleadoId) {
        return ServiceFacadeLocator.getInstanceFacadeEmpleado().tieneAsignacionesPendientes(empleadoId);
    }

    public Empleado findById(int empleadoId) {
        try {
            Empleado empleado = ServiceFacadeLocator.getInstanceFacadeEmpleado().findById(empleadoId);

            return empleado;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Empleado> getAllEmpleadosDisponibles() {
        return ServiceFacadeLocator.getInstanceFacadeEmpleado().getAllEmpleadosDisponibles();
    }
}
