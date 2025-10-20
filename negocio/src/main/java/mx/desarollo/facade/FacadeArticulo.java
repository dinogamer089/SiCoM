package mx.desarollo.facade;

import mx.desarollo.entity.Articulo;
import mx.desarollo.delegate.DelegateArticulo;

public class FacadeArticulo {
    private final DelegateArticulo delegateArticulo;

    public FacadeArticulo() {
        this.delegateArticulo = new DelegateArticulo();
    }

    public java.util.List<Articulo> obtenerArticulos(){
        return delegateArticulo.findAllArticulos();
    }

    public void guardarArticulo(Articulo articulo){
        delegateArticulo.saveArticulo(articulo);
    }

    public void eliminarArticulo(Integer id) {
        System.out.println("=== FacadeArticulo.eliminarArticulo() ===");
        System.out.println("ID: " + id);
        try {
            delegateArticulo.deleteArticulo(id);
            System.out.println("✓ Artículo eliminado en Facade");
        } catch (Exception e) {
            System.err.println("ERROR en FacadeArticulo.eliminarArticulo():");
            e.printStackTrace();
            throw e;
        }
    }
    public void modificarArticulo(Articulo art) {
        delegateArticulo.updateArticulo(art);
    }

}