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

    private Articulo articuloEditar;

    /**
     * Constructor por defecto del bean de articulo en la capa UI.
     * Inicializa el helper que se comunicara con la capa de negocio.
     */
    public ArticuloBeanUI() {
        articuloHelper = new ArticuloHelper();
    }

    /**
     * Metodo de inicializacion que se ejecuta despues de construir el bean.
     * Prepara la lista de articulos y el objeto para alta.
     * @Throws Si ocurre un error al obtener los articulos desde el helper.
     */
    @PostConstruct
    public void init() {
        articulo = new Articulo();
        articulos = articuloHelper.obtenerTodas();
        nuevoArticulo = new Articulo();
        nuevoArticulo.setActivo(true);
        imagenBytes = null;
        imagenMime = null;
    }

    /**
     * Metodo para obtener la lista de articulos mostrados en la tabla de administracion.
     * @return Una lista de objetos Articulo.
     */
    public List<Articulo> getArticulos() { return articulos; }

    /**
     * Metodo para seleccionar un articulo desde la tabla de la vista.
     * Guarda la referencia para operaciones posteriores como eliminacion.
     * @Params Objeto de tipo Articulo art
     */
    public void seleccionar(Articulo art) {
        this.seleccionada = art;
        System.out.println("Seleccionada: " + art.getNombre());
    }

    /**
     * Metodo getter para obtener el articulo actualmente seleccionado.
     * @return El objeto Articulo seleccionado o null si no hay seleccion.
     */
    public Articulo getSeleccionada() { return seleccionada; }

    /**
     * Metodo setter para establecer el articulo seleccionado desde la vista.
     * @Params Objeto de tipo Articulo art
     */
    public void setSeleccionada(Articulo art) { this.seleccionada = art; }

    /**
     * Metodo para obtener la imagen de un articulo en formato base64 lista para usarse en un tag img.
     * @Throws Si ocurre un error al codificar la imagen en base64.
     * @Params Objeto de tipo Articulo art
     * @return Una cadena con la Data URL de la imagen o una cadena vacia si no hay imagen.
     */
    public String getImagenBase64(Articulo art) {
        if (art == null || art.getImagen() == null || art.getImagen().getDatos() == null) {
            return "";
        }
        byte[] imageBytes = art.getImagen().getDatos();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mime = art.getImagen().getMime();
        return "data:" + mime + ";base64," + base64;
    }

    /**
     * Metodo getter para el objeto nuevoArticulo usado en el formulario de alta.
     * @return El objeto Articulo que se esta creando.
     */
    public Articulo getNuevoArticulo() { return nuevoArticulo; }

    /**
     * Metodo setter para asignar un nuevoArticulo desde la vista.
     * @Params Objeto de tipo Articulo nuevoArticulo
     */
    public void setNuevoArticulo(Articulo nuevoArticulo) { this.nuevoArticulo = nuevoArticulo; }

    /**
     * Metodo getter para obtener el precio del nuevo articulo.
     * @return El precio capturado para el nuevo articulo.
     */
    public BigDecimal getNuevoPrecio() { return nuevoPrecio; }

    /**
     * Metodo setter para asignar el precio del nuevo articulo.
     * @Params Objeto de tipo BigDecimal nuevoPrecio
     */
    public void setNuevoPrecio(BigDecimal nuevoPrecio) { this.nuevoPrecio = nuevoPrecio; }

    /**
     * Metodo manejador del evento de subida de archivos (PrimeFaces).
     * Valida el tipo y extension del archivo y guarda los bytes de la imagen en memoria.
     * @Throws Si el archivo es invalido o ocurre un error durante el procesamiento.
     * @Params Objeto de tipo FileUploadEvent event
     */
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

    /**
     * Metodo para guardar un nuevo articulo usando los datos capturados en el formulario.
     * Valida precio, imagen y luego persiste articulo e imagen en una sola transaccion.
     * @Throws Si faltan datos requeridos, la imagen no se cargo o la base de datos rechaza la operacion.
     */
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

    /**
     * Metodo para limpiar el formulario de alta de articulos.
     * Reinicia el articulo nuevo, el precio y los buffers de imagen.
     */
    public void cancelarAlta() {
        this.nuevoArticulo = new Articulo();
        this.nuevoArticulo.setActivo(true);
        this.nuevoPrecio = null;
        this.imagenBytes = null;
        this.imagenMime = null;
    }

    /**
     * Metodo para eliminar el articulo actualmente seleccionado.
     * Valida que haya una seleccion, elimina por ID y refresca la lista.
     * @Throws Si no hay articulo seleccionado, la base de datos rechaza la eliminacion
     *         o el articulo esta siendo referenciado por rentas/cotizaciones.
     */
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

    public void guardarModificacion() {
        try {
            if (seleccionada == null) return;

            if (imagenBytes != null && imagenBytes.length > 0) {
                Imagen nuevaImg = new Imagen();
                nuevaImg.setDatos(imagenBytes);
                nuevaImg.setMime(imagenMime != null ? imagenMime : "image/jpeg");
                seleccionada.setImagen(nuevaImg);
            }

            articuloHelper.actualizar(seleccionada);

            articulos = articuloHelper.obtenerTodas();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Artículo modificado correctamente"));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo modificar el artículo"));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public Articulo getArticuloEditar() { return articuloEditar; }
    public void setArticuloEditar(Articulo articuloEditar) { this.articuloEditar = articuloEditar; }
}
