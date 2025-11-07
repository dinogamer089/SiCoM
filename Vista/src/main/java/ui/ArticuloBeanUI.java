package ui;

import helper.ArticuloHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Named("articuloUI")
@SessionScoped
public class ArticuloBeanUI implements Serializable {
    private Articulo articulo;
    private List<Articulo> articulos;
    private ArticuloHelper articuloHelper;
    private Articulo seleccionada;

    private Articulo nuevoArticulo;
    private BigDecimal nuevoPrecio;
    private UploadedFile uploadedFile;

    public ArticuloBeanUI() {
        articuloHelper = new ArticuloHelper();
    }

    @PostConstruct
    public void init() {
        articulo = new Articulo();
        articulos = articuloHelper.obtenerTodas();
        nuevoArticulo = new Articulo();
        nuevoArticulo.setActivo(true);
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

    public void setSeleccionada(Articulo art) {
        this.seleccionada = art;
    }

    public String getImagenBase64(Articulo art) {
        if (art == null || art.getImagen() == null || art.getImagen().getDatos() == null) {
            return "";
        }
        byte[] imageBytes = art.getImagen().getDatos();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mime = art.getImagen().getMime();
        return "data:" + mime + ";base64," + base64;
    }

    public Articulo getNuevoArticulo() {
        return nuevoArticulo;
    }

    public void setNuevoArticulo(Articulo nuevoArticulo) {
        this.nuevoArticulo = nuevoArticulo;
    }

    public BigDecimal getNuevoPrecio() {
        return nuevoPrecio;
    }

    public void setNuevoPrecio(BigDecimal nuevoPrecio) {
        this.nuevoPrecio = nuevoPrecio;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public void guardarNuevo() {
        try {
            if (uploadedFile == null || uploadedFile.getContent() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe seleccionar una imagen"));
                return;
            }

            nuevoArticulo.setPrecio(nuevoPrecio);

            Imagen imagen = new Imagen();
            imagen.setDatos(uploadedFile.getContent());
            imagen.setMime(uploadedFile.getContentType());

            articuloHelper.guardarConImagen(nuevoArticulo, imagen);

            articulos = articuloHelper.obtenerTodas();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Artículo creado correctamente"));

            nuevoArticulo = new Articulo();
            nuevoArticulo.setActivo(true);
            nuevoPrecio = null;
            uploadedFile = null;

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar el artículo"));
        }
    }
}