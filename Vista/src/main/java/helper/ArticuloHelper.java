
package helper;

import mx.desarollo.entity.Articulo;
import mx.avanti.desarollo.integration.ServiceLocator;
import java.util.List;

public class ArticuloHelper {

    public List<Articulo> obtenerTodas() {
        System.out.println("=== ArticuloHelper.obtenerTodas() ===");
        try {
            List<Articulo> articulos = ServiceLocator.getInstanceArticuloDAO().obtenerTodos();
            System.out.println("✓ Artículos obtenidos: " + (articulos != null ? articulos.size() : "null"));
            return articulos;
        } catch (Exception e) {
            System.err.println("✗ ERROR en ArticuloHelper:");
            System.err.println("  Tipo: " + e.getClass().getName());
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}