package mx.desarollo.delegate;

import mx.avanti.desarollo.dao.EmpleadoDAO;
import mx.avanti.desarollo.dao.RentaDAO;
import mx.desarollo.entity.Empleado;
import mx.avanti.desarollo.integration.ServiceLocator;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class DelegateEmpleado {

    private final EmpleadoDAO empleadoDAO;
    private final RentaDAO rentaDAO;

    public DelegateEmpleado() {
        this.empleadoDAO = ServiceLocator.getInstanceEmpleadoDAO();
        this.rentaDAO = ServiceLocator.getInstanceRentaDAO();
    }

    /**
     * Obtiene todos los empleados de la base de datos.
     */
    public List<Empleado> getAllEmpleados() {
        return empleadoDAO.findAll();
    }

    /**
     * Guarda un nuevo empleado, encriptando su contraseña.
     */
    public void saveEmpleado(Empleado empleado) {
        // Encripta la contraseña antes de guardar
        String hashed = BCrypt.hashpw(empleado.getContrasena(), BCrypt.gensalt(12));
        empleado.setContrasena(hashed);
        empleadoDAO.save(empleado);
    }

    /**
     * Actualiza un empleado existente.
     * Si la contraseña se modifico (no es el hash existente), la encripta.
     */
    public void updateEmpleado(Empleado empleado) {

        Optional<Empleado> existingOpt = empleadoDAO.find(empleado.getId());
        if (existingOpt.isPresent()) {
            String contrasenaNueva = empleado.getContrasena();
            String contrasenaAntigua = existingOpt.get().getContrasena();


            if (contrasenaNueva != null && !contrasenaNueva.equals(contrasenaAntigua)) {
                if (!contrasenaNueva.startsWith("$2")) {
                    String hashed = BCrypt.hashpw(contrasenaNueva, BCrypt.gensalt(12));
                    empleado.setContrasena(hashed);
                }
            }
        } else {
            String hashed = BCrypt.hashpw(empleado.getContrasena(), BCrypt.gensalt(12));
            empleado.setContrasena(hashed);
        }

        empleadoDAO.update(empleado);
    }

    /**
     * Elimina un empleado de la base de datos.
     */
    public void deleteEmpleado(Empleado empleado) {
        if (empleado != null && empleado.getId() != null) {
            boolean tieneAsignaciones = rentaDAO.existeAsignacionPendiente(empleado.getId());
            if (tieneAsignaciones) {
                throw new IllegalStateException("No se puede eliminar el empleado ya que tiene asignaciones pendientes.");
            }
        }
        empleadoDAO.delete(empleado);
    }

    public Empleado findById(Integer id) {
        return ServiceLocator.getInstanceEmpleadoDAO().findById(id);
    }

    public List<Empleado> findAllEmpleadosDisponibles(){
        return ServiceLocator.getInstanceEmpleadoDAO().obtenerEmpleadosDisponibles();
    }

    /**
     * Valida si un empleado tiene asignaciones pendientes.
     * @param empleadoId id del empleado
     * @return true si tiene rentas en reparto o recoleccion, false en caso contrario
     */
    public boolean tieneAsignacionesPendientes(Integer empleadoId){
        return rentaDAO.existeAsignacionPendiente(empleadoId);
    }
}
