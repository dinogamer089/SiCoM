package ui;

import helper.CombinacionMesaHelper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Categoria;
import mx.desarollo.entity.CombinacionMesa;
import mx.desarollo.entity.Forma;
import mx.desarollo.entity.Imagen;
import mx.desarollo.entity.TextilTipo;
import mx.desarollo.facade.FacadeArticulo;
import mx.desarollo.integration.ServiceFacadeLocator;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Named("combinacionMesaAdminUI")
@ViewScoped
public class CombinacionMesaAdminBeanUI implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CombinacionMesaHelper helper = new CombinacionMesaHelper();

    private List<CombinacionMesa> combinaciones;
    private CombinacionMesa seleccionada;

    private Integer mesaId;
    private Integer mantelId;
    private Integer caminoId;
    private Integer cubreId;

    private List<Articulo> mesasDisponibles;
    private List<Articulo> mantelesDisponibles;
    private List<Articulo> caminosDisponibles;
    private List<Articulo> cubresDisponibles;

    private byte[] imagenBytes;
    private String imagenMime;

    /** Id de la combinación seleccionada */
    private Integer selId;

    /**
     * Metodo de inicializacion del bean de administracion de combinaciones.
     * Carga las combinaciones existentes y las mesas disponibles para armar nuevas combinaciones.
     * @Throws Si la base de datos rechaza las consultas iniciales o hay errores en la capa de negocio.
     */
    @PostConstruct
    public void init() {
        refrescarLista();
        cargarMesasDisponibles();
        mantelesDisponibles = new ArrayList<>();
        caminosDisponibles = new ArrayList<>();
        cubresDisponibles = new ArrayList<>();
    }

    /**
     * Metodo privado para refrescar la lista de combinaciones desde el helper.
     * @Throws Si la base de datos rechaza la consulta de todas las combinaciones.
     */
    private void refrescarLista() {
        combinaciones = helper.obtenerTodas();
    }

    /**
     * Metodo privado para cargar las mesas disponibles a partir de los articulos.
     * Solo se consideran articulos cuya categoria es MESA.
     * @Throws Si la base de datos rechaza la consulta de articulos.
     */
    private void cargarMesasDisponibles() {
        FacadeArticulo f = ServiceFacadeLocator.getInstanceFacadeArticulo();
        List<Articulo> todos = f.obtenerArticulos();
        mesasDisponibles = todos.stream()
                .filter(a -> a.getCategoria() == Categoria.MESA)
                .collect(Collectors.toList());
    }

    // ==== Textiles según mesa ====

    /**
     * Metodo que se ejecuta cuando cambia la mesa seleccionada en el formulario.
     * Reinicia seleccion de textiles, limpia buffers de imagen y carga textiles compatibles
     * (manteles por forma exacta, caminos y cubres por forma o UNIVERSAL).
     * @Throws Si la base de datos rechaza la consulta de articulos o hay error al filtrar.
     */
    public void onMesaChange() {
        mantelId = null;
        caminoId = null;
        cubreId = null;
        imagenBytes = null;
        imagenMime = null;

        Articulo mesaSeleccionada = resolveArticulo(mesaId).orElse(null);
        if (mesaSeleccionada == null || mesaSeleccionada.getForma() == null) {
            mantelesDisponibles = new ArrayList<>();
            caminosDisponibles = new ArrayList<>();
            cubresDisponibles = new ArrayList<>();
            return;
        }

        Forma forma = mesaSeleccionada.getForma();
        FacadeArticulo f = ServiceFacadeLocator.getInstanceFacadeArticulo();
        List<Articulo> todos = f.obtenerArticulos();

        // Manteles forma exacta
        mantelesDisponibles = todos.stream()
                .filter(a -> a.getCategoria() == Categoria.TEXTIL
                        && a.getTextilTipo() == TextilTipo.MANTEL
                        && a.getForma() == forma)
                .collect(Collectors.toList());

        // Caminos / Cubres: misma forma o UNIVERSAL
        caminosDisponibles = todos.stream()
                .filter(a -> a.getCategoria() == Categoria.TEXTIL
                        && a.getTextilTipo() == TextilTipo.CAMINO
                        && formaCompatible(a, forma))
                .collect(Collectors.toList());

        cubresDisponibles = todos.stream()
                .filter(a -> a.getCategoria() == Categoria.TEXTIL
                        && a.getTextilTipo() == TextilTipo.CUBRE
                        && formaCompatible(a, forma))
                .collect(Collectors.toList());
    }

    /**
     * Metodo privado para evaluar si la forma de un articulo textil
     * es compatible con la forma de la mesa (misma o UNIVERSAL).
     * @Params Objeto de tipo Articulo a, Objeto de tipo Forma formaMesa
     * @return true si la forma es igual a la de la mesa o UNIVERSAL; false en otro caso.
     */
    private boolean formaCompatible(Articulo a, Forma formaMesa) {
        if (a == null || formaMesa == null || a.getForma() == null) return false;
        return a.getForma() == formaMesa || a.getForma() == Forma.UNIVERSAL;
    }

    /**
     * Metodo que se ejecuta cuando cambia el mantel seleccionado.
     * Reinicia los extras (camino y cubre) para obligar a elegirlos nuevamente.
     */
    public void onMantelChange() {
        caminoId = null;
        cubreId = null;
    }

    /**
     * Metodo que se ejecuta cuando cambia el camino seleccionado.
     * Si se elige un camino, se limpia el cubre para mantener la exclusividad de un solo extra.
     */
    public void onCaminoChange() {
        // Si selecciona un camino, limpiamos cubre para mantener exclusividad
        if (caminoId != null) {
            cubreId = null;
        }
    }

    /**
     * Metodo que se ejecuta cuando cambia el cubremantel seleccionado.
     * Si se elige un cubre, se limpia el camino para mantener la exclusividad de un solo extra.
     */
    public void onCubreChange() {
        // Si selecciona un cubre, limpiamos camino para mantener exclusividad
        if (cubreId != null) {
            caminoId = null;
        }
    }

    // ==== Upload de imagen ====

    /**
     * Metodo manejador del evento de subida de imagen de combinacion (PrimeFaces).
     * Guarda el contenido y el mimeType en campos temporales del bean.
     * @Throws Si ocurre un error de E/S al leer el archivo subido.
     * @Params Objeto de tipo FileUploadEvent event
     */
    public void handleFileUpload(FileUploadEvent event) throws IOException {
        UploadedFile file = event.getFile();
        if (file != null) {
            imagenBytes = file.getContent();
            imagenMime = file.getContentType();
        }
    }

    // ==== Guardar combinación ====

    /**
     * Metodo para guardar una nueva combinacion de mesa desde la UI.
     * Valida mesa, mantel, imagen y construye el objeto CombinacionMesa
     * para delegarlo al helper/fachada que aplican las reglas de negocio.
     * @Throws Si faltan datos requeridos, las reglas de negocio se violan
     *         o la base de datos rechaza la operacion de guardado.
     */
    public void guardarCombinacion() {
        var ctx = jakarta.faces.context.FacesContext.getCurrentInstance();

        if (mesaId == null || mantelId == null) {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Mesa y mantel son obligatorios", null));
            return;
        }

        if (imagenBytes == null || imagenMime == null) {
            cargarImagenDesdeSesion();
        }
        if (imagenBytes == null || imagenBytes.length == 0 || imagenMime == null) {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Debe subir una imagen para la combinación", null));
            return;
        }

        Articulo mesa = resolveArticulo(mesaId).orElse(null);
        Articulo mantel = resolveArticulo(mantelId).orElse(null);
        Articulo camino = (caminoId != null) ? resolveArticulo(caminoId).orElse(null) : null;
        Articulo cubre = (cubreId != null) ? resolveArticulo(cubreId).orElse(null) : null;

        Imagen imagen = new Imagen();
        imagen.setDatos(imagenBytes);
        imagen.setMime(imagenMime);

        CombinacionMesa c = new CombinacionMesa();
        c.setMesa(mesa);
        c.setMantel(mantel);
        c.setCamino(camino);
        c.setCubre(cubre);
        c.setImagen(imagen);
        c.setActivo(true);

        try {
            helper.guardar(c);
            refrescarLista();
            limpiarFormulario();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Combinación guardada", null));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
            ctx.validationFailed();
        }
    }

    /**
     * Metodo invocado desde el boton Cancelar del dialogo de alta
     * para limpiar todos los campos del formulario.
     */
    public void cancelarAlta() {
        limpiarFormulario();
    }

    /**
     * Metodo privado para limpiar los campos del formulario de alta/edicion de combinaciones.
     * Reinicia IDs, listas de textiles y buffers de imagen.
     */
    private void limpiarFormulario() {
        mesaId = null;
        mantelId = null;
        caminoId = null;
        cubreId = null;
        mantelesDisponibles = new ArrayList<>();
        caminosDisponibles = new ArrayList<>();
        cubresDisponibles = new ArrayList<>();
        imagenBytes = null;
        imagenMime = null;
    }

    // ==== Eliminar ====

    /**
     * Metodo para eliminar la combinacion actualmente seleccionada en la tabla.
     * @Throws Si no hay combinacion seleccionada, la base de datos rechaza la eliminacion
     *         o la combinacion no puede borrarse por estar en uso.
     */
    public void eliminarSeleccionada() {
        var ctx = jakarta.faces.context.FacesContext.getCurrentInstance();

        if (seleccionada != null && seleccionada.getId() != null) {
            Integer id = seleccionada.getId();
            System.out.println("Solicitado eliminar combinación id=" + id);
            try {
                helper.eliminarPorId(id);
                seleccionada = null;
                selId = null;
                refrescarLista();
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Combinación eliminada", null));
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                ex.getMessage(), null));
                ctx.validationFailed();
            }
        } else {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Seleccione una combinación", null));
            ctx.validationFailed();
        }
    }

    /**
     * Metodo para eliminar una combinacion por ID tomado de los parametros de la peticion.
     * Pensado para invocarse desde comandos remotos en la UI.
     * @Throws Si no se recibe el id de combinacion, o la base de datos rechaza la eliminacion.
     */
    public void eliminarPorId() {
        var ctx = jakarta.faces.context.FacesContext.getCurrentInstance();
        try {
            var params = ctx.getExternalContext().getRequestParameterMap();
            String sid = params.get("comboId");
            if (sid == null || sid.isBlank()) {
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Sin id de combinación", null));
                ctx.validationFailed();
                return;
            }
            Integer id = Integer.valueOf(sid);
            System.out.println("[remote] Eliminar combinación id=" + id);
            helper.eliminarPorId(id);
            seleccionada = null;
            selId = null;
            refrescarLista();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Combinación eliminada", null));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
            ctx.validationFailed();
        }
    }

    // ==== Selección en tabla ====

    /**
     * Metodo para seleccionar una combinacion desde la tabla de la vista.
     * Guarda el objeto y su ID para uso posterior.
     * @Params Objeto de tipo CombinacionMesa c
     */
    public void seleccionar(CombinacionMesa c) {
        seleccionada = c;
        selId = (c != null ? c.getId() : null);
        try {
            Integer id = (c != null) ? c.getId() : null;
            String mesa = (c != null && c.getMesa() != null) ? c.getMesa().getNombre() : "";
            if (id != null) {
                System.out.println("Seleccionada combinación id=" + id +
                        (mesa.isEmpty() ? "" : (" — " + mesa)));
            }
        } catch (Exception ignored) { }
    }

    /**
     * Metodo para limpiar la seleccion actual de combinacion.
     * Deja selId y seleccionada en null.
     */
    public void limpiarSeleccion() {
        seleccionada = null;
        selId = null;
    }

    // ==== Getters / Setters para la vista ====

    /**
     * Metodo getter para obtener la lista de combinaciones mostradas en la tabla.
     * @return Lista de objetos CombinacionMesa.
     */
    public List<CombinacionMesa> getCombinaciones() {
        return combinaciones;
    }

    /**
     * Metodo getter para obtener la combinacion actualmente seleccionada.
     * @return Objeto CombinacionMesa seleccionado o null.
     */
    public CombinacionMesa getSeleccionada() {
        return seleccionada;
    }

    /**
     * Metodo setter para establecer la combinacion seleccionada desde la vista.
     * @Params Objeto de tipo CombinacionMesa seleccionada
     */
    public void setSeleccionada(CombinacionMesa seleccionada) {
        this.seleccionada = seleccionada;
    }

    /**
     * Metodo getter para obtener el ID de la mesa seleccionada en el formulario.
     * @return Objeto Integer con el ID de la mesa o null.
     */
    public Integer getMesaId() {
        return mesaId;
    }

    /**
     * Metodo setter para asignar el ID de la mesa seleccionada.
     * @Params Objeto de tipo Integer mesaId
     */
    public void setMesaId(Integer mesaId) {
        this.mesaId = mesaId;
    }

    /**
     * Metodo getter para obtener el ID del mantel seleccionado.
     * @return Objeto Integer con el ID del mantel o null.
     */
    public Integer getMantelId() {
        return mantelId;
    }

    /**
     * Metodo setter para asignar el ID del mantel seleccionado.
     * @Params Objeto de tipo Integer mantelId
     */
    public void setMantelId(Integer mantelId) {
        this.mantelId = mantelId;
    }

    /**
     * Metodo getter para obtener el ID del camino seleccionado.
     * @return Objeto Integer con el ID del camino o null.
     */
    public Integer getCaminoId() {
        return caminoId;
    }

    /**
     * Metodo setter para asignar el ID del camino seleccionado.
     * @Params Objeto de tipo Integer caminoId
     */
    public void setCaminoId(Integer caminoId) {
        this.caminoId = caminoId;
    }

    /**
     * Metodo getter para obtener el ID del cubremantel seleccionado.
     * @return Objeto Integer con el ID del cubre o null.
     */
    public Integer getCubreId() {
        return cubreId;
    }

    /**
     * Metodo setter para asignar el ID del cubremantel seleccionado.
     * @Params Objeto de tipo Integer cubreId
     */
    public void setCubreId(Integer cubreId) {
        this.cubreId = cubreId;
    }

    /**
     * Metodo getter para obtener la lista de mesas disponibles.
     * @return Lista de objetos Articulo con categoria MESA.
     */
    public List<Articulo> getMesasDisponibles() {
        return mesasDisponibles;
    }

    /**
     * Metodo getter para obtener la lista de manteles disponibles.
     * @return Lista de objetos Articulo para manteles compatibles.
     */
    public List<Articulo> getMantelesDisponibles() {
        return mantelesDisponibles;
    }

    /**
     * Metodo getter para obtener la lista de caminos disponibles.
     * @return Lista de objetos Articulo para caminos compatibles.
     */
    public List<Articulo> getCaminosDisponibles() {
        return caminosDisponibles;
    }

    /**
     * Metodo getter para obtener la lista de cubres disponibles.
     * @return Lista de objetos Articulo para cubremanteles compatibles.
     */
    public List<Articulo> getCubresDisponibles() {
        return cubresDisponibles;
    }

    /**
     * Metodo getter para obtener el ID de la combinacion seleccionada.
     * @return Objeto Integer con el ID de la combinacion o null.
     */
    public Integer getSelId() {
        return selId;
    }

    /**
     * Metodo setter para asignar el ID de la combinacion seleccionada.
     * @Params Objeto de tipo Integer selId
     */
    public void setSelId(Integer selId) {
        this.selId = selId;
    }

    /**
     * Metodo para obtener la miniatura en Data URL de una combinacion.
     * @Params Objeto de tipo CombinacionMesa c
     * @return Cadena con el Data URL de la imagen o null si no existe imagen.
     */
    public String getMiniatura(CombinacionMesa c) {
        return helper.toDataUrl(c);
    }

    /**
     * Alias usado en miniatura(c) desde el XHTML.
     * @Params Objeto de tipo CombinacionMesa c
     * @return Cadena con el Data URL de la imagen o null si no existe imagen.
     */
    public String miniatura(CombinacionMesa c) {
        return getMiniatura(c);
    }

    // ==== Utilerías internas ====

    /**
     * Metodo privado para resolver un articulo por ID usando la fachada.
     * @Params Objeto de tipo Integer id
     * @return Optional con el articulo encontrado o vacio si no existe.
     */
    private Optional<Articulo> resolveArticulo(Integer id) {
        if (id == null) return Optional.empty();
        return ServiceFacadeLocator.getInstanceFacadeArticulo().obtenerArticuloPorId(id);
    }

    /**
     * Metodo privado para cargar la imagen desde la sesion en caso de que
     * haya sido subida por un servlet/flujo alterno.
     * Elimina los atributos de sesion una vez cargados en el bean.
     */
    private void cargarImagenDesdeSesion() {
        try {
            var ctx = jakarta.faces.context.FacesContext.getCurrentInstance();
            var sessionMap = ctx.getExternalContext().getSessionMap();
            Object bytes = sessionMap.get("uploadBytes");
            Object mime = sessionMap.get("uploadMime");
            if (bytes instanceof byte[] && mime instanceof String) {
                imagenBytes = (byte[]) bytes;
                imagenMime = (String) mime;
                sessionMap.remove("uploadBytes");
                sessionMap.remove("uploadMime");
            }
        } catch (Exception ignored) { }
    }
}
