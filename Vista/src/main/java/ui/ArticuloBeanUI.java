package ui;

import helper.ArticuloHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.desarollo.entity.Articulo;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
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
    private UploadedFile imagenFile;

    public ArticuloBeanUI() {
        articuloHelper = new ArticuloHelper();
    }

    @PostConstruct
    public void init() {
        articulo = new Articulo();
        nuevoArticulo = new Articulo();
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

    public void setSeleccionada(Articulo art) {
        this.seleccionada = art;
    }

    public Articulo getNuevoArticulo() {
        return nuevoArticulo;
    }

    public void setNuevoArticulo(Articulo nuevoArticulo) {
        this.nuevoArticulo = nuevoArticulo;
    }

    public UploadedFile getImagenFile() {
        return imagenFile;
    }

    public void setImagenFile(UploadedFile imagenFile) {
        this.imagenFile = imagenFile;
    }

    // Método para preparar nuevo artículo
    public void prepararNuevoArticulo() {
        nuevoArticulo = new Articulo();
        imagenFile = null;
    }

    // Método para guardar artículo
    public void guardarArticulo() {
        try {
            if (nuevoArticulo.getNombre() == null || nuevoArticulo.getNombre().trim().isEmpty()) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "El nombre es obligatorio");
                return;
            }

            if (nuevoArticulo.getCantidad() == null || nuevoArticulo.getCantidad() <= 0) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "La cantidad debe ser mayor a 0");
                return;
            }

            if (nuevoArticulo.getPrecio() == null || nuevoArticulo.getPrecio() <= 0) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "El precio debe ser mayor a 0");
                return;
            }

            if (nuevoArticulo.getTipo() == null || nuevoArticulo.getTipo().trim().isEmpty()) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "El tipo es obligatorio");
                return;
            }


            if (imagenFile != null && imagenFile.getContent() != null && imagenFile.getSize() > 0) {
                try {
                    byte[] imageBytes = imagenFile.getContent();
                    nuevoArticulo.setImagen(imageBytes);
                    System.out.println("Imagen procesada: " + imageBytes.length + " bytes");
                } catch (Exception e) {
                    System.err.println("Error al procesar imagen: " + e.getMessage());
                    e.printStackTrace();
                    mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "No se pudo procesar la imagen");
                }
            } else {
                System.out.println("No se subió imagen");
            }

            articuloHelper.guardarArticulo(nuevoArticulo);
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Artículo creado correctamente");

            // Recargar lista
            articulos = articuloHelper.obtenerTodas();

            // Limpiar formulario
            nuevoArticulo = new Articulo();
            imagenFile = null;

        } catch (Exception e) {
            System.err.println("Error al guardar artículo: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el artículo: " + e.getMessage());
        }
    }

    // Método para eliminar artículo
    public void eliminarArticulo() {
        try {
            if (seleccionada == null) {
                mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar un artículo para eliminar");
                return;
            }

            System.out.println("Eliminando artículo: " + seleccionada.getNombre() + " (ID: " + seleccionada.getIdarticulo() + ")");

            articuloHelper.eliminarArticulo(seleccionada.getIdarticulo());
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Artículo eliminado correctamente");

            // Recargar lista
            articulos = articuloHelper.obtenerTodas();

            // Limpiar selección
            seleccionada = null;

        } catch (Exception e) {
            System.err.println("Error al eliminar artículo: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el artículo: " + e.getMessage());
        }
    }



    public String getImagenBase64(byte[] imagen) {
        if (imagen == null || imagen.length == 0) {
            return "";
        }
        try {
            String base64 = Base64.getEncoder().encodeToString(imagen);
            return "data:image/jpeg;base64," + base64;
        } catch (Exception e) {
            System.err.println("Error al convertir imagen a base64: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, resumen, detalle));
    }
}