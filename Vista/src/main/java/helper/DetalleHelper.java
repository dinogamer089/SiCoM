package helper;

import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.util.ArrayList;
import java.util.List;

public class DetalleHelper {
    public List<Detallerenta> obtenerTodas() {
        System.out.println("=== DetalleHelper.obtenerTodas() ===");
        try {
            List<Detallerenta> detalles = ServiceFacadeLocator.getInstanceFacadeDetalle().obtenerDetalles();
            System.out.println("✓ Detalles de renta obtenidos: " + (detalles != null ? detalles.size() : "null"));

            // Si es null, retornar lista vacía en lugar de null
            if (detalles == null) {
                return new ArrayList<>();
            }

            return detalles;
        } catch (Exception e) {
            System.err.println("✗ ERROR en DetalleHelper:");
            System.err.println("  Tipo: " + e.getClass().getName());
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, retornar lista vacía en lugar de lanzar excepción
            return new ArrayList<>();
        }
    }

    public void guardarDetalle(Detallerenta detalle) {
        System.out.println("=== DetalleHelper.crearDetalle() ===");
        try {
            ServiceFacadeLocator.getInstanceFacadeDetalle().guardarDetalle(detalle);
            System.out.println("✓ Detalle creado exitosamente");
        } catch (Exception e) {
            System.err.println("✗ ERROR al crear Detalle:");
            e.printStackTrace();
            throw e;
        }
    }
}
