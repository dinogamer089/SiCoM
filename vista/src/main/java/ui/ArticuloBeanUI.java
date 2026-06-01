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
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Named("articuloUI")
@SessionScoped
public class ArticuloBeanUI implements Serializable {

    private Articulo articulo;
    private List<Articulo> articulos;
    private List<Articulo> articulosOriginales; // Lista sin filtrar
    private final ArticuloHelper articuloHelper;
    private Articulo seleccionada;

    private Articulo nuevoArticulo;
    private BigDecimal nuevoPrecio;

    // buffer de imagen subida (advanced)
    private byte[] imagenBytes;
    private String imagenMime;
    // buffer manejado por servlet de subida

    private Articulo articuloEditar;

    // Filtros para búsqueda
    private String filtroNombre = "";
    private String filtroTipo = "";

    // Paginación
    private int paginaActual = 1;
    private int registrosPorPagina = 5;

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
        articulosOriginales = articuloHelper.obtenerTodas();
        articulos = articulosOriginales; // Inicialmente muestra todos
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
                enviarRespuestaJS("error", "El archivo está vacío");
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
                enviarRespuestaJS("error", "Solo se acepta JPG o PNG.");
                return;
            }

            this.imagenBytes = file.getContent();
            this.imagenMime = (ct.startsWith("image/jp")) ? "image/jpeg" : "image/png";

            enviarRespuestaJS("exito", "Imagen cargada: " + name);

        } catch (Exception ex) {
            ex.printStackTrace();
            enviarRespuestaJS("error", "No se pudo procesar la imagen");
        }
    }

    /**
     * Metodo para guardar un nuevo articulo usando los datos capturados en el formulario.
     * Valida precio, imagen y luego persiste articulo e imagen en una sola transaccion.
     * @Throws Si faltan datos requeridos, la imagen no se cargo o la base de datos rechaza la operacion.
     */
    public void guardarNuevo() {
        try {
            // 1 Validar Nombre
            if (nuevoArticulo.getNombre() == null || nuevoArticulo.getNombre().trim().isEmpty()) {
                enviarRespuestaJS("error", "El nombre es obligatorio.");
                return;
            }

            // 2 Validar Categoría
            if (nuevoArticulo.getCategoria() == null) {
                enviarRespuestaJS("error", "La categoría es obligatoria.");
                return;
            }

            // 3 Validar Precio
            if (nuevoPrecio == null) {
                enviarRespuestaJS("error", "El precio es obligatorio.");
                return;
            }
            if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
                enviarRespuestaJS("error", "El precio debe ser mayor a 0.");
                return;
            }

            // 4 Validar Unidades
            if (nuevoArticulo.getUnidades() == null) {
                enviarRespuestaJS("error", "Las unidades son obligatorias.");
                return;
            }
            if (nuevoArticulo.getUnidades() < 0) {
                enviarRespuestaJS("error", "Las unidades deben ser 0 o mayores.");
                return;
            }

            // 5 Validar Imagen
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
                enviarRespuestaJS("error", "Es obligatorio subir una imagen (JPG/PNG).");
                return;
            }

            // --- Guardado ---

            nuevoArticulo.setPrecio(nuevoPrecio);

            Imagen imagen = new Imagen();
            imagen.setDatos(imagenBytes);
            imagen.setMime(imagenMime);

            articuloHelper.guardarConImagen(nuevoArticulo, imagen);

            // refrescar tabla
            articulosOriginales = articuloHelper.obtenerTodas();
            filtrarArticulos(); // Aplicar filtros actuales

            cancelarAlta();

            var map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            map.remove("uploadBytes");
            map.remove("uploadMime");

            enviarRespuestaJS("exito", "¡Artículo creado correctamente!");

        } catch (Exception e) {
            e.printStackTrace();
            enviarRespuestaJS("error", "Ocurrió un error interno al guardar.");
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

        var map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        map.remove("uploadBytes");
        map.remove("uploadMime");
    }

    /**
     * Metodo para eliminar el articulo actualmente seleccionado.
     * Valida que haya una seleccion, elimina por ID y refresca la lista.
     * @Throws Si no hay articulo seleccionado, la base de datos rechaza la eliminacion
     *         o el articulo esta siendo referenciado por rentas/cotizaciones.
     */
    public void eliminarSeleccionada() {
        try {
            if (seleccionada == null || seleccionada.getId() == null) {
                enviarRespuestaJS("error", "Debe seleccionar un artículo para eliminar");
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            articuloHelper.eliminarPorId(seleccionada.getId());

            // refrescar lista y limpiar selección
            articulosOriginales = articuloHelper.obtenerTodas();
            filtrarArticulos(); // Aplicar filtros actuales
            seleccionada = null;

            enviarRespuestaJS("exito", "Artículo eliminado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            enviarRespuestaJS("error", "No se pudo eliminar. Puede estar referenciado por rentas o cotizaciones");
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public void guardarModificacion() {
        try {
            if (seleccionada == null) return;

            // --- VALIDACIONES MANUALES PARA MODIFICACION ---

            // 1 Validar Nombre
            if (seleccionada.getNombre() == null || seleccionada.getNombre().trim().isEmpty()) {
                articulos = articuloHelper.obtenerTodas();
                enviarRespuestaJS("error", "El nombre es obligatorio.");
                return;
            }

            // 2 Validar Categoría
            if (seleccionada.getCategoria() == null) {
                articulos = articuloHelper.obtenerTodas();
                enviarRespuestaJS("error", "La categoría es obligatoria.");
                return;
            }

            // 3 Validar Precio
            if (seleccionada.getPrecio() == null) {
                articulos = articuloHelper.obtenerTodas();
                enviarRespuestaJS("error", "El precio es obligatorio.");
                return;
            }
            if (seleccionada.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                articulos = articuloHelper.obtenerTodas();
                enviarRespuestaJS("error", "El precio debe ser mayor a 0.");
                return;
            }

            // 4 Validar Unidades
            if (seleccionada.getUnidades() == null) {
                articulos = articuloHelper.obtenerTodas();
                enviarRespuestaJS("error", "Las unidades son obligatorias.");
                return;
            }
            if (seleccionada.getUnidades() < 0) {
                articulos = articuloHelper.obtenerTodas();
                enviarRespuestaJS("error", "Las unidades deben ser 0 o mayores.");
                return;
            }

            // 5 Imagen (Opcional en modificación)
            if (imagenBytes == null || imagenBytes.length == 0) {
                var map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
                byte[] content = (byte[]) map.get("uploadBytes");
                String ct = (String) map.get("uploadMime");

                if (content != null && content.length > 0) {
                    this.imagenBytes = content;
                    if (ct != null && !ct.isBlank()) {
                        this.imagenMime = ct.toLowerCase();
                    } else {
                        boolean looksJpeg = content.length >= 2 && (content[0] & 0xFF) == 0xFF && (content[1] & 0xFF) == 0xD8;
                        this.imagenMime = looksJpeg ? "image/jpeg" : "image/png";
                    }
                }
            }

            if (imagenBytes != null && imagenBytes.length > 0) {
                Imagen nuevaImg = new Imagen();
                nuevaImg.setDatos(imagenBytes);
                nuevaImg.setMime(imagenMime != null ? imagenMime : "image/jpeg");

                seleccionada.setImagen(nuevaImg);
            }

            articuloHelper.actualizar(seleccionada);
            articulosOriginales = articuloHelper.obtenerTodas();
            filtrarArticulos(); // Aplicar filtros actuales

            this.imagenBytes = null;
            this.imagenMime = null;
            var map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            map.remove("uploadBytes");
            map.remove("uploadMime");

            enviarRespuestaJS("exito", "Artículo modificado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            articulosOriginales = articuloHelper.obtenerTodas();
            filtrarArticulos(); // Aplicar filtros actuales
            enviarRespuestaJS("error", "No se pudo modificar el artículo: " + e.getMessage());
        }
    }

    /**
     * Metodo actualizado para revertir cambios en memoria si el usuario
     * modificó campos pero luego decidió cancelar.
     */
    public void cancelarModificacion() {
        // Limpia buffers
        this.imagenBytes = null;
        this.imagenMime = null;
        var map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        map.remove("uploadBytes");
        map.remove("uploadMime");

        // Esto arregla que la tabla se quede con datos editados si se da clic en Cancelar
        articulosOriginales = articuloHelper.obtenerTodas();
        filtrarArticulos(); // Aplicar filtros actuales
    }

    private void enviarRespuestaJS(String tipo, String mensaje) {
        PrimeFaces.current().ajax().addCallbackParam("tipoRespuesta", tipo);
        PrimeFaces.current().ajax().addCallbackParam("mensajeRespuesta", mensaje);
    }

    /**
     * Metodo getter para obtener el filtro de nombre.
     * @return El texto del filtro de nombre.
     */
    public String getFiltroNombre() {
        return filtroNombre;
    }

    /**
     * Metodo setter para establecer el filtro de nombre.
     * @param filtroNombre El texto para filtrar por nombre.
     */
    public void setFiltroNombre(String filtroNombre) {
        this.filtroNombre = filtroNombre;
    }

    /**
     * Metodo getter para obtener el filtro de tipo.
     * @return El texto del filtro de tipo.
     */
    public String getFiltroTipo() {
        return filtroTipo;
    }

    /**
     * Metodo setter para establecer el filtro de tipo.
     * @param filtroTipo El tipo para filtrar articulos.
     */
    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
    }

    /**
     * Metodo para filtrar la lista de articulos por nombre y/o tipo.
     * Se ejecuta cuando el usuario modifica los filtros en la vista.
     */
    public void filtrarArticulos() {
        // Refrescar la lista original desde la base de datos
        articulosOriginales = articuloHelper.obtenerTodas();

        // Aplicar filtros
        articulos = articulosOriginales.stream()
                .filter(art -> {
                    boolean cumpleNombre = true;
                    boolean cumpleTipo = true;

                    // Filtro por nombre
                    if (filtroNombre != null && !filtroNombre.trim().isEmpty()) {
                        cumpleNombre = art.getNombre() != null &&
                                art.getNombre().toLowerCase().contains(filtroNombre.toLowerCase().trim());
                    }

                    // Filtro por tipo (usando categoria del articulo)
                    if (filtroTipo != null && !filtroTipo.trim().isEmpty()) {
                        cumpleTipo = matchTipo(art.getCategoria(), filtroTipo);
                    }

                    return cumpleNombre && cumpleTipo;
                })
                .toList();

        // Resetear a la primera página al filtrar
        paginaActual = 1;
    }

    /**
     * Metodo auxiliar para mapear el filtro de tipo a la categoria del articulo.
     * @param categoria La categoria enum del articulo (MESA, TEXTIL, SILLA, etc.)
     * @param filtro El filtro seleccionado (Silla, Mesa, Mantel, etc.)
     * @return true si la categoria coincide con el filtro
     */
    private boolean matchTipo(mx.desarollo.entity.Categoria categoria, String filtro) {
        if (categoria == null || filtro == null) return false;

        String filt = filtro.toUpperCase().trim();

        // Mapeo directo para categorias simples
        if (categoria == mx.desarollo.entity.Categoria.SILLA && filt.equals("SILLA")) return true;
        if (categoria == mx.desarollo.entity.Categoria.MESA && filt.equals("MESA")) return true;
        if (categoria == mx.desarollo.entity.Categoria.CARPA && filt.equals("CARPA")) return true;
        if (categoria == mx.desarollo.entity.Categoria.COOLER && filt.equals("COOLER")) return true;
        if (categoria == mx.desarollo.entity.Categoria.CALENTON && filt.equals("CALENTON")) return true;

        // Para TEXTIL, necesitamos revisar el textilTipo
        // Como no tenemos acceso directo al textilTipo aquí, usaremos el nombre
        // o podemos asumir que TEXTIL cubre Mantel, Camino y Cubremantel
        if (categoria == mx.desarollo.entity.Categoria.TEXTIL) {
            if (filt.equals("MANTEL") || filt.equals("CAMINO") || filt.equals("CUBREMANTEL")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Metodo para obtener la lista de articulos paginados segun la pagina actual.
     * @return Lista de articulos de la pagina actual
     */
    public List<Articulo> getArticulosPaginados() {
        if (articulos == null || articulos.isEmpty()) {
            return List.of();
        }

        int inicio = (paginaActual - 1) * registrosPorPagina;
        int fin = Math.min(inicio + registrosPorPagina, articulos.size());

        if (inicio >= articulos.size()) {
            paginaActual = 1;
            inicio = 0;
            fin = Math.min(registrosPorPagina, articulos.size());
        }

        return articulos.subList(inicio, fin);
    }

    /**
     * Metodo para obtener el numero de pagina actual.
     * @return Numero de pagina actual
     */
    public int getPaginaActual() {
        return paginaActual;
    }

    /**
     * Metodo para establecer el numero de pagina actual.
     * @param paginaActual Numero de pagina
     */
    public void setPaginaActual(int paginaActual) {
        this.paginaActual = paginaActual;
    }

    /**
     * Metodo para obtener el total de paginas disponibles.
     * @return Total de paginas
     */
    public int getTotalPaginas() {
        if (articulos == null || articulos.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) articulos.size() / registrosPorPagina);
    }

    /**
     * Metodo para obtener el total de articulos filtrados.
     * @return Total de articulos
     */
    public int getTotalArticulos() {
        return articulos != null ? articulos.size() : 0;
    }

    /**
     * Metodo para obtener el numero de registro inicial de la pagina actual.
     * @return Numero de registro inicial
     */
    public int getRegistroInicio() {
        if (articulos == null || articulos.isEmpty()) {
            return 0;
        }
        return (paginaActual - 1) * registrosPorPagina + 1;
    }

    /**
     * Metodo para obtener el numero de registro final de la pagina actual.
     * @return Numero de registro final
     */
    public int getRegistroFin() {
        if (articulos == null || articulos.isEmpty()) {
            return 0;
        }
        return Math.min(paginaActual * registrosPorPagina, articulos.size());
    }

    /**
     * Metodo para ir a la primera pagina.
     */
    public void irPrimeraPagina() {
        paginaActual = 1;
    }

    /**
     * Metodo para ir a la pagina anterior.
     */
    public void irPaginaAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
        }
    }

    /**
     * Metodo para ir a la pagina siguiente.
     */
    public void irPaginaSiguiente() {
        if (paginaActual < getTotalPaginas()) {
            paginaActual++;
        }
    }

    /**
     * Metodo para ir a la ultima pagina.
     */
    public void irUltimaPagina() {
        paginaActual = getTotalPaginas();
    }
}