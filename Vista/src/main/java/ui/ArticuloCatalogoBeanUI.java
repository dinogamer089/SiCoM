package ui;

import helper.ArticuloHelper;
import helper.dto.ArticuloCardDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import mx.desarollo.entity.Categoria;
import mx.desarollo.entity.Forma;
import mx.desarollo.facade.FacadeArticulo;
import mx.desarollo.integration.ServiceFacadeLocator;

@Named("articuloCatalogoUI")
@SessionScoped
public class ArticuloCatalogoBeanUI implements Serializable {

    private static final long serialVersionUID = 1L;

    // Catálogo completo
    private List<ArticuloCardDTO> articulos;

    // Grupos para la columna izquierda
    private List<ArticuloCardDTO> mesasRedondas;
    private List<ArticuloCardDTO> mesasRectangulares;
    private List<ArticuloCardDTO> sillasGarden;
    private List<ArticuloCardDTO> sillasLifetime;
    private List<ArticuloCardDTO> carpasChicas;
    private List<ArticuloCardDTO> carpasGrandes;
    private List<ArticuloCardDTO> coolers;
    private List<ArticuloCardDTO> calentones;

    // Selección actual para el Preview
    private ArticuloCardDTO seleccionado;

    // Cantidad a agregar (ligado al <p:inputNumber>)
    private int cantidad = 1;

    // Fecha seleccionada por el cliente
    private LocalDate fechaSeleccionada;

    @Inject
    private CarritoBeanUI carritoBeanUI;

    /**
     * Metodo de inicializacion del bean de catalogo.
     * Inicializa las listas vacias. La carga real ocurre cuando el usuario selecciona fecha.
     */
    @PostConstruct
    public void init() {
        this.articulos = new ArrayList<>();
        buildGroups();
    }

    /**
     * Metodo utilitario para obtener la fecha actual del sistema.
     * Se usa en la vista (p:datePicker) para bloquear fechas pasadas (mindate).
     * @return Fecha actual (LocalDate).
     */
    public LocalDate getNow() {
        return LocalDate.now();
    }

    /**
     * Metodo llamado cuando el usuario selecciona una fecha en el DatePicker.
     * Recarga el catalogo calculando disponibilidad real (Total - Reservado) para ese dia.
     * Si la fecha es valida, actualiza las listas y muestra mensaje de confirmacion.
     */
    public void onFechaCambio() {
        if (fechaSeleccionada == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe seleccionar una fecha."));
            return;
        }

        // 1. Validar que sea fecha futura o presente
        if (fechaSeleccionada.isBefore(LocalDate.now())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Fecha inválida", "No puedes reservar en el pasado."));
            this.articulos = new ArrayList<>();
            buildGroups();
            return;
        }

        // 2. Cargar catalogo calculando stock RESTANDO reservas para la fecha seleccionada
        FacadeArticulo facade = ServiceFacadeLocator.getInstanceFacadeArticulo();
        var entidades = facade.listarCatalogoCliente();

        // Usamos el Helper modificado que recibe la fecha
        this.articulos = ArticuloHelper.toCardDTOs(entidades, fechaSeleccionada);

        // 3. Reconstruir los grupos visuales
        buildGroups();

        // 4. Resetear seleccion
        this.seleccionado = null;
        this.cantidad = 1;

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Fecha Establecida", "Catálogo actualizado para: " + fechaSeleccionada));
    }

    /**
     * Metodo que se ejecuta al hacer clic en una tarjeta de articulo.
     * Actualiza el articulo seleccionado para el preview y reinicia la cantidad.
     * @Params Objeto de tipo ArticuloCardDTO dto
     */
    public void ver(ArticuloCardDTO dto) {
        this.seleccionado = dto;
        this.cantidad = 1;
    }

    /**
     * Metodo alias para compatibilidad con el XHTML que invoque #{...seleccionar(dto)}.
     * Internamente delega en el metodo ver(dto).
     * @Params Objeto de tipo ArticuloCardDTO dto
     */
    public void seleccionar(ArticuloCardDTO dto) {
        ver(dto);
    }

    /**
     * Metodo que se invoca al presionar el boton "Agregar" debajo del preview.
     * Agrega al carrito la cantidad seleccionada del articulo, respetando el stock maximo.
     * @Throws Si ocurre un error en la logica del carrito o la cantidad es invalida.
     */
    public void agregarSeleccion() {
        if (seleccionado != null && seleccionado.isDisponible()) {
            int n = Math.min(Math.max(1, cantidad), getMaxCantidad());
            for (int i = 0; i < n; i++) {
                carritoBeanUI.agregar(seleccionado);
            }
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Agregado", n + " articulos al carrito."));
        }
    }

    /**
     * Metodo para indicar si el boton de "Agregar" debe estar deshabilitado.
     * @return true si no hay articulo seleccionado, no esta disponible o no hay fecha; false en caso contrario.
     */
    public boolean isAgregarDeshabilitado() {
        return (seleccionado == null) || !seleccionado.isDisponible() || fechaSeleccionada == null;
    }

    /**
     * Busca un articulo en la lista cargada (que ya tiene stock calculado) por su ID.
     * Usado por la vista (XHTML) para mostrar detalles ricos (precio, stock) de manteles/caminos seleccionados.
     * @param id ID del articulo a buscar
     * @return El DTO encontrado o null si no existe.
     */
    public ArticuloCardDTO buscarPorId(Integer id) {
        if (id == null || articulos == null) return null;
        return articulos.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /* ============ Agrupaciones ============ */

    /**
     * Metodo privado para construir las listas de articulos agrupados
     * por categoria y forma, utilizadas en la columna izquierda del catalogo.
     * @Throws Si la lista de articulos es nula o hay error al filtrar.
     */
    private void buildGroups() {
        if (articulos == null) articulos = Collections.emptyList();

        var mesas = filtrarPorCategoria(Categoria.MESA);
        mesasRedondas = filtrarPorForma(mesas, Forma.REDONDA);
        mesasRectangulares = filtrarPorForma(mesas, Forma.RECTANGULAR);

        var sillas = filtrarPorCategoria(Categoria.SILLA);
        sillasGarden = filtrarPorNombreContiene(sillas, "garden");
        sillasLifetime = filtrarPorNombreContiene(sillas, "lifetime");

        var carpas = filtrarPorCategoria(Categoria.CARPA);
        carpasGrandes = filtrarPorNombreContiene(carpas, "grande");
        carpasChicas = carpas.stream()
                .filter(a -> !nombreContiene(a, "grande"))
                .collect(Collectors.toList());

        coolers = filtrarPorCategoria(Categoria.COOLER);
        calentones = filtrarPorCategoria(Categoria.CALENTON);

        nullSafe();
    }

    /**
     * Metodo privado para asegurar que las listas de grupos no sean nulas.
     * En caso de ser nulas, se inicializan como listas vacias.
     */
    private void nullSafe() {
        if (mesasRedondas == null) mesasRedondas = Collections.emptyList();
        if (mesasRectangulares == null) mesasRectangulares = Collections.emptyList();
        if (sillasGarden == null) sillasGarden = Collections.emptyList();
        if (sillasLifetime == null) sillasLifetime = Collections.emptyList();
        if (carpasChicas == null) carpasChicas = Collections.emptyList();
        if (carpasGrandes == null) carpasGrandes = Collections.emptyList();
        if (coolers == null) coolers = Collections.emptyList();
        if (calentones == null) calentones = Collections.emptyList();
    }

    /**
     * Metodo privado para filtrar articulos del catalogo por categoria.
     * @Params Objeto de tipo Categoria categoria
     * @return Una lista de ArticuloCardDTO que pertenecen a la categoria dada.
     */
    private List<ArticuloCardDTO> filtrarPorCategoria(Categoria categoria) {
        return articulos.stream().filter(a -> a.getCategoria() == categoria).collect(Collectors.toList());
    }

    /**
     * Metodo privado para filtrar una lista de articulos por forma.
     * @Params Lista de ArticuloCardDTO fuente, Objeto de tipo Forma forma
     * @return Una lista de ArticuloCardDTO con la forma indicada.
     */
    private List<ArticuloCardDTO> filtrarPorForma(List<ArticuloCardDTO> fuente, Forma forma) {
        return fuente.stream().filter(a -> a.getForma() == forma).collect(Collectors.toList());
    }

    /**
     * Metodo privado para filtrar una lista de articulos cuyo nombre contiene una palabra clave.
     * @Params Lista de ArticuloCardDTO fuente, Objeto de tipo String kw
     * @return Una lista de ArticuloCardDTO cuyos nombres contienen la palabra clave.
     */
    private List<ArticuloCardDTO> filtrarPorNombreContiene(List<ArticuloCardDTO> fuente, String kw) {
        String k = kw.toLowerCase(Locale.ROOT);
        return fuente.stream().filter(a -> nombreContiene(a, k)).collect(Collectors.toList());
    }

    /**
     * Metodo privado de apoyo para verificar si el nombre de un articulo
     * contiene una palabra clave (sin sensibilidad a mayusculas/minusculas).
     * @Params Objeto de tipo ArticuloCardDTO a, String kw
     * @return true si el nombre contiene la palabra clave, false en caso contrario.
     */
    private boolean nombreContiene(ArticuloCardDTO a, String kw) {
        String n = Optional.ofNullable(a.getNombre()).orElse("");
        return n.toLowerCase(Locale.ROOT).contains(kw);
    }

    /* ============ Getters/Setters ============ */

    public LocalDate getFechaSeleccionada() {
        return fechaSeleccionada;
    }

    public void setFechaSeleccionada(LocalDate fechaSeleccionada) {
        this.fechaSeleccionada = fechaSeleccionada;
    }

    /**
     * Metodo getter para obtener la lista de mesas redondas del catalogo.
     * @return Lista de ArticuloCardDTO para mesas redondas.
     */
    public List<ArticuloCardDTO> getMesasRedondas() { return mesasRedondas; }

    /**
     * Metodo getter para obtener la lista de mesas rectangulares del catalogo.
     * @return Lista de ArticuloCardDTO para mesas rectangulares.
     */
    public List<ArticuloCardDTO> getMesasRectangulares() { return mesasRectangulares; }

    /**
     * Metodo getter para obtener la lista de sillas tipo Garden.
     * @return Lista de ArticuloCardDTO para sillas Garden.
     */
    public List<ArticuloCardDTO> getSillasGarden() { return sillasGarden; }

    /**
     * Metodo getter para obtener la lista de sillas tipo Lifetime.
     * @return Lista de ArticuloCardDTO para sillas Lifetime.
     */
    public List<ArticuloCardDTO> getSillasLifetime() { return sillasLifetime; }

    /**
     * Metodo getter para obtener la lista de carpas chicas.
     * @return Lista de ArticuloCardDTO para carpas chicas.
     */
    public List<ArticuloCardDTO> getCarpasChicas() { return carpasChicas; }

    /**
     * Metodo getter para obtener la lista de carpas grandes.
     * @return Lista de ArticuloCardDTO para carpas grandes.
     */
    public List<ArticuloCardDTO> getCarpasGrandes() { return carpasGrandes; }

    /**
     * Metodo getter para obtener la lista de coolers.
     * @return Lista de ArticuloCardDTO para coolers.
     */
    public List<ArticuloCardDTO> getCoolers() { return coolers; }

    /**
     * Metodo getter para obtener la lista de calentones.
     * @return Lista de ArticuloCardDTO para calentones.
     */
    public List<ArticuloCardDTO> getCalentones() { return calentones; }

    /**
     * Metodo getter para obtener el articulo actualmente seleccionado para el preview.
     * @return Objeto ArticuloCardDTO seleccionado o null si no hay seleccion.
     */
    public ArticuloCardDTO getSeleccionado() { return seleccionado; }

    /**
     * Metodo getter para obtener la cantidad elegida en el inputNumber.
     * @return Entero con la cantidad seleccionada.
     */
    public int getCantidad() { return cantidad; }

    /**
     * Metodo setter para establecer la cantidad elegida por el usuario.
     * La cantidad se limita entre 1 y el stock disponible del articulo.
     * @Params Objeto de tipo int cantidad
     */
    public void setCantidad(int cantidad) {
        int max = getMaxCantidad();
        if (max <= 0) {
            this.cantidad = 1;
        } else {
            this.cantidad = Math.max(1, Math.min(cantidad, max));
        }
    }

    /**
     * Metodo para obtener la cantidad maxima seleccionable segun el stock del articulo.
     * @return Entero con el numero maximo de unidades disponibles, o 0 si no hay articulo.
     */
    public int getMaxCantidad() {
        return (seleccionado != null) ? Math.max(0, seleccionado.getUnidades()) : 0;
    }
}