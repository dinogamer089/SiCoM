package ui;

import helper.ArticuloHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Articulo;
import java.io.Serializable; import java.util.List;

@Named("articuloUI")
@SessionScoped
public class ArticuloBeanUI implements Serializable {
    private Articulo articulo;
    private List<Articulo> articulos;
    private ArticuloHelper articuloHelper;
    public ArticuloBeanUI() {
        articuloHelper = new ArticuloHelper();
    }
    private Articulo seleccionada;

    @PostConstruct public void init() {
        articulo = new Articulo();
        articulos = articuloHelper.obtenerTodas();
    }

    public List<Articulo> getArticulos() {
        return articulos;
    }

    public void seleccionar(Articulo art) {
        this.seleccionada = art;
        System.out.println("Seleccionada: " + art.getNombre());
    }

    public Articulo getSeleccionada() {
        return seleccionada;
    }

    public void setSeleccionada(Articulo art){
        this.seleccionada = art;
    }
}