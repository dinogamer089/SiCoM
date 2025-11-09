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
}
