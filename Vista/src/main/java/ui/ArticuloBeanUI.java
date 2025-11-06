package ui;

import helper.ArticuloHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;
import org.primefaces.event.FileUploadEvent;
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
    private UploadedFile archivoImagen;

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

    public UploadedFile getArchivoImagen() {
        return archivoImagen;
    }

    public void setArchivoImagen(UploadedFile archivoImagen) {
        this.archivoImagen = archivoImagen;
    }

    public void handleFileUpload(FileUploadEvent event) {
        this.archivoImagen = event.getFile();
    }

    public void guardarNuevo() {
        System.out.println("=== INICIANDO GUARDAR NUEVO ===");
        try {
            // Validar campos obligatorios
            if (nuevoArticulo.getNombre() == null || nuevoArticulo.getNombre().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El campo Nombre es obligatorio"));
                return;
            }

            if (nuevoArticulo.getCategoria() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El campo Categoría es obligatorio"));
                return;
            }

            if (nuevoPrecio == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El campo Precio es obligatorio"));
                return;
            }

            if (nuevoArticulo.getUnidades() == null || nuevoArticulo.getUnidades() <= 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El campo Unidades es obligatorio y debe ser mayor a 0"));
                return;
            }

            if (archivoImagen == null || archivoImagen.getContent() == null || archivoImagen.getContent().length == 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe seleccionar una imagen"));
                return;
            }

            System.out.println("Nombre: " + nuevoArticulo.getNombre());
            System.out.println("Categoría: " + nuevoArticulo.getCategoria());
            System.out.println("Precio: " + nuevoPrecio);
            System.out.println("Unidades: " + nuevoArticulo.getUnidades());
            System.out.println("Tamaño de imagen: " + archivoImagen.getContent().length + " bytes");

            nuevoArticulo.setPrecio(nuevoPrecio);

            Imagen imagen = new Imagen();
            imagen.setDatos(archivoImagen.getContent());
            imagen.setMime(archivoImagen.getContentType());

            System.out.println("Guardando en base de datos...");
            ServiceLocator.getInstanceArticuloDAO().saveWithImage(nuevoArticulo, imagen);

            System.out.println("Actualizando lista de artículos...");
            articulos = articuloHelper.obtenerTodas();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Artículo creado correctamente"));

            nuevoArticulo = new Articulo();
            nuevoArticulo.setActivo(true);
            nuevoPrecio = null;
            archivoImagen = null;

            System.out.println("=== GUARDAR COMPLETADO CON ÉXITO ===");

        } catch (Exception e) {
            System.out.println("=== ERROR AL GUARDAR ===");
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar el artículo: " + e.getMessage()));
        }
    }
}
