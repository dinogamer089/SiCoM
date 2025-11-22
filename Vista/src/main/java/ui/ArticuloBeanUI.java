package ui;

import helper.ArticuloHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;
import org.primefaces.event.FileUploadEvent;

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

    // buffer de imagen subida (advanced)
    private byte[] imagenBytes;
    private String imagenMime;
    // buffer manejado por servlet de subida

    public ArticuloBeanUI() {
        articuloHelper = new ArticuloHelper();
    }

    @PostConstruct
    public void init() {
        articulo = new Articulo();
        articulos = articuloHelper.obtenerTodas();
        nuevoArticulo = new Articulo();
        nuevoArticulo.setActivo(true);
        imagenBytes = null;
        imagenMime = null;
    }

    public List<Articulo> getArticulos() { return articulos; }

    public void seleccionar(Articulo art) {
        this.seleccionada = art;
        System.out.println("Seleccionada: " + art.getNombre());
    }

    public Articulo getSeleccionada() { return seleccionada; }
    public void setSeleccionada(Articulo art) { this.seleccionada = art; }

    public String getImagenBase64(Articulo art) {
        if (art == null || art.getImagen() == null || art.getImagen().getDatos() == null) {
            return "";
        }
        byte[] imageBytes = art.getImagen().getDatos();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mime = art.getImagen().getMime();
        return "data:" + mime + ";base64," + base64;
    }

    public Articulo getNuevoArticulo() { return nuevoArticulo; }
    public void setNuevoArticulo(Articulo nuevoArticulo) { this.nuevoArticulo = nuevoArticulo; }

    public BigDecimal getNuevoPrecio() { return nuevoPrecio; }
    public void setNuevoPrecio(BigDecimal nuevoPrecio) { this.nuevoPrecio = nuevoPrecio; }

    // La imagen se obtiene de sesión cargada por UploadImageServlet

    // === Manejo de upload (PrimeFaces advanced) ===
    public void onUpload(FileUploadEvent event) {
        try {
            var file = event.getFile();
            if (file == null || file.getContent() == null || file.getContent().length == 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El archivo está vacío"));
                return;
            }

            String ct = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
            String name = file.getFileName() != null ? file.getFileName() : "";
            String ext = "";
            int p = name.lastIndexOf('.');
            if (p >= 0 && p < name.length() - 1) {
                ext = name.substring(p + 1).toLowerCase();
            }

            boolean mimeOk = ct.equals("image/jpeg") || ct.equals("image/jpg") || ct.equals("image/pjpeg")
                    || ct.equals("image/png") || ct.equals("image/x-png");
            boolean extOk = ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png");

            if (!mimeOk || !extOk) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Formato no permitido",
                                "Solo se acepta JPG o PNG."));
                return;
            }

            this.imagenBytes = file.getContent();
            this.imagenMime = (ct.startsWith("image/jp")) ? "image/jpeg" : "image/png";

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "Imagen cargada: " + name));

        } catch (Exception ex) {
            ex.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo procesar la imagen"));
        }
    }

    public void guardarNuevo() {
        try {
            if (nuevoPrecio == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El precio es requerido"));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }
            // Si no hubo evento, leer desde sesión (cargado por UploadImageServlet)
            if (imagenBytes == null || imagenBytes.length == 0 || imagenMime == null) {
                var map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
                byte[] content = (byte[]) map.get("uploadBytes");
                String ct = (String) map.get("uploadMime");
                if (content != null && content.length > 0) {
                    this.imagenBytes = content;
                    boolean looksJpeg = content.length >= 2 && (content[0] & 0xFF) == 0xFF && (content[1] & 0xFF) == 0xD8;
                    this.imagenMime = (ct != null && !ct.isBlank()) ? ct.toLowerCase() : (looksJpeg ? "image/jpeg" : "image/png");
                }
            }
            if (imagenBytes == null || imagenBytes.length == 0 || imagenMime == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe cargar una imagen JPG/PNG"));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            nuevoArticulo.setPrecio(nuevoPrecio);

            Imagen imagen = new Imagen();
            imagen.setDatos(imagenBytes);
            imagen.setMime(imagenMime);

            // Persistimos en una sola transacción (helper->DAO.saveWithImage)
            articuloHelper.guardarConImagen(nuevoArticulo, imagen);

            // refrescar tabla
            articulos = articuloHelper.obtenerTodas();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Artículo creado correctamente"));

            // limpiar buffers/form
            cancelarAlta();
            // limpiar archivo simple
            var map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            map.remove("uploadBytes");
            map.remove("uploadMime");

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar el artículo"));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public void cancelarAlta() {
        this.nuevoArticulo = new Articulo();
        this.nuevoArticulo.setActivo(true);
        this.nuevoPrecio = null;
        this.imagenBytes = null;
        this.imagenMime = null;
    }

    // === Eliminación ===
    public void eliminarSeleccionada() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            if (seleccionada == null || seleccionada.getId() == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Seleccione un artículo", "Debe seleccionar un artículo para eliminar"));
                context.validationFailed();
                return;
            }

            articuloHelper.eliminarPorId(seleccionada.getId());

            // refrescar lista y limpiar selección
            articulos = articuloHelper.obtenerTodas();
            seleccionada = null;

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Eliminado", "Artículo eliminado correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo eliminar", "Puede estar referenciado por rentas o cotizaciones"));
            context.validationFailed();
        }
    }
}
