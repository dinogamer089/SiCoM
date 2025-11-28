package helper;

import mx.desarollo.entity.Renta;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.util.ArrayList;
import java.util.List;

public class RentaHelper {
    public List<Renta> obtenerTodasCotizaciones() {
        System.out.println("=== RentaHelper.obtenerTodasCotizaciones() ===");
        try {
            List<Renta> rentas = ServiceFacadeLocator.getInstanceFacadeRenta().obtenerCotizaciones();
            System.out.println("✓ Rentas obtenidos: " + (rentas != null ? rentas.size() : "null"));

            // Si es null, retornar lista vacía en lugar de null
            if (rentas == null) {
                return new ArrayList<>();
            }

            return rentas;
        } catch (Exception e) {
            System.err.println("✗ ERROR en RentaHelper:");
            System.err.println("  Tipo: " + e.getClass().getName());
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, retornar lista vacía en lugar de lanzar excepción
            return new ArrayList<>();
        }
    }

    public Renta findById(int rentaId) {
        try {
            Renta renta = ServiceFacadeLocator.getInstanceFacadeRenta().obtenerRentaId(rentaId);

            return renta;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean cambiarEstado(Integer idRenta, String nuevoEstado) {
        System.out.println("=== RentaHelper.cambiarEstado() ===");
        try {
            ServiceFacadeLocator.getInstanceFacadeRenta().cambiarEstado(idRenta, nuevoEstado);
            System.out.println("✓ Estado cambiado correctamente vía Stored Procedure.");
            return true;
        } catch (Exception e) {
            System.err.println("✗ ERROR al cambiar estado:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Renta> obtenerTodasRentas() {
        System.out.println("=== RentaHelper.obtenerTodasRentas() ===");
        try {
            List<Renta> rentas = ServiceFacadeLocator.getInstanceFacadeRenta().obtenerRentas();
            System.out.println("✓ Rentas obtenidos: " + (rentas != null ? rentas.size() : "null"));

            // Si es null, retornar lista vacía en lugar de null
            if (rentas == null) {
                return new ArrayList<>();
            }

            return rentas;
        } catch (Exception e) {
            System.err.println("✗ ERROR en RentaHelper:");
            System.err.println("  Tipo: " + e.getClass().getName());
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, retornar lista vacía en lugar de lanzar excepción
            return new ArrayList<>();
        }
    }

    public void actualizarRenta(Renta renta) throws Exception {
        System.out.println("=== RentaHelper.actualizarRenta() ===");
        try {
            ServiceFacadeLocator.getInstanceFacadeRenta().actualizarRenta(renta);
            System.out.println("✓ Renta actualizada correctamente.");
        } catch (Exception e) {
            System.err.println("✗ ERROR al actualizar renta en RentaHelper:");
            e.printStackTrace();
        }
    }

    /**
     * Metodo para obtener las rentas disponibles y asignadas validando el empleado.
     * Gestiona errores internamente devolviendo una lista vacia en caso de fallo.
     * @Throws Si ocurre una excepcion durante la llamada al facade, se captura y retorna lista vacia.
     * @Params Objeto de tipo Integer idEmpleado
     * @return Una lista de objetos Renta, nunca retorna nulo.
     */
    public List<Renta> obtenerRentasDisponiblesYAsignadas(Integer idEmpleado) {
        if (idEmpleado == null) return new ArrayList<>();
        try {
            List<Renta> lista = ServiceFacadeLocator.getInstanceFacadeRenta().obtenerRentasDisponiblesYAsignadas(idEmpleado);
            return (lista != null) ? lista : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Metodo que implementa la maquina de estados para calcular el siguiente estatus de una renta.
     * Define la transicion logica del ciclo de vida de la renta segun su estado actual.
     * @Throws No lanza excepciones, retorna nulo si el estado no tiene transicion definida.
     * @Params Objeto de tipo String estadoActual
     * @return Un String con el nombre del siguiente estado o nulo si no aplica.
     */
    public String calcularSiguienteEstado(String estadoActual) {
        if (estadoActual == null) return null;
        switch (estadoActual) {
            case "Confirmado":              return "Pendiente a reparto";
            case "Pendiente a reparto":     return "En reparto";      // Aquí el empleado se la asigna
            case "En reparto":              return "Entregado";       // Aquí llega al cliente

            // CORRECCION: El empleado se detiene aquí. El admin debe moverlo a Pendiente a recoleccion
            case "Entregado":               return null;

            case "Pendiente a recoleccion": return "En recoleccion";  // Aquí el empleado se la asigna (retorno)
            case "En recoleccion":          return "Finalizada";      // Fin del ciclo
            default:                        return null;
        }
    }
}