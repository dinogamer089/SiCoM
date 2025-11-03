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

    public void guardarRenta(Renta renta) {
        System.out.println("=== RentaHelper.crearRenta() ===");
        try {
            ServiceFacadeLocator.getInstanceFacadeRenta().guardarRenta(renta);
            System.out.println("✓ Renta creado exitosamente");
        } catch (Exception e) {
            System.err.println("✗ ERROR al crear Renta:");
            e.printStackTrace();
            throw e;
        }
    }
}
