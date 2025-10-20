package helper;

import mx.desarollo.entity.Articulo;
import mx.desarollo.integration.ServiceFacadeLocator;
import java.util.List;
import java.util.ArrayList;

public class ArticuloHelper {

    public List<Articulo> obtenerTodas() {
        System.out.println("=== ArticuloHelper.obtenerTodas() ===");
        try {
            List<Articulo> articulos = ServiceFacadeLocator.getInstanceFacadeArticulo().obtenerArticulos();
            System.out.println("✓ Artículos obtenidos: " + (articulos != null ? articulos.size() : "null"));

            // Si es null, retornar lista vacía en lugar de null
            if (articulos == null) {
                return new ArrayList<>();
            }

            return articulos;
        } catch (Exception e) {
            System.err.println("✗ ERROR en ArticuloHelper:");
            System.err.println("  Tipo: " + e.getClass().getName());
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, retornar lista vacía en lugar de lanzar excepción
            return new ArrayList<>();
        }
    }

    public void guardarArticulo(Articulo articulo) {
        System.out.println("=== ArticuloHelper.crearArticulo() ===");
        try {
            ServiceFacadeLocator.getInstanceFacadeArticulo().guardarArticulo(articulo);
            System.out.println("✓ Artículo creado exitosamente");
        } catch (Exception e) {
            System.err.println("✗ ERROR al crear artículo:");
            e.printStackTrace();
            throw e;
        }
    }

    public void eliminarArticulo(Integer id) {
        System.out.println("=== ArticuloHelper.eliminarArticulo() ===");
        System.out.println("ID a eliminar: " + id);
        try {
            ServiceFacadeLocator.getInstanceFacadeArticulo().eliminarArticulo(id);
            System.out.println("✓ Artículo eliminado exitosamente");
        } catch (Exception e) {
            System.err.println("✗ ERROR al eliminar artículo:");
            e.printStackTrace();
            throw e;
        }
    }
}