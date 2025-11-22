package ui;

import helper.ArticuloHelper;
import helper.dto.ArticuloCardDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import mx.desarollo.entity.Categoria;
import mx.desarollo.entity.Forma;
import mx.desarollo.facade.FacadeArticulo;
import mx.desarollo.integration.ServiceFacadeLocator;

@Named("articuloCatalogoUI")
@ViewScoped
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

    @Inject
    private CarritoBeanUI carritoBeanUI;

    @PostConstruct
    public void init() {
        FacadeArticulo facade = ServiceFacadeLocator.getInstanceFacadeArticulo();
        var entidades = facade.listarCatalogoCliente();
        this.articulos = ArticuloHelper.toCardDTOs(entidades);
        buildGroups();
        if (!articulos.isEmpty()) {
            this.seleccionado = articulos.get(0);
            this.cantidad = 1; // arranque sano
        }
    }

    /* ============ Interacción ============ */

    /** Al hacer click en una tarjeta de la izquierda, solo cambiamos el preview y reseteamos cantidad. */
    public void ver(ArticuloCardDTO dto) {
        this.seleccionado = dto;
        this.cantidad = 1;
    }

    /** Alias por compatibilidad con XHTML que invoque #{...seleccionar(dto)} */
    public void seleccionar(ArticuloCardDTO dto) {
        ver(dto);
    }

    /** Botón Agregar (debajo del preview). */
    public void agregarSeleccion() {
        if (seleccionado != null && seleccionado.isDisponible()) {
            // Si tu CarritoBeanUI ya soporta cantidad, usa: carritoBeanUI.agregar(seleccionado, cantidad);
            // Fallback compatible: agregar N veces
            int n = Math.min(Math.max(1, cantidad), getMaxCantidad());
            for (int i = 0; i < n; i++) {
                carritoBeanUI.agregar(seleccionado);
            }
        }
    }

    public boolean isAgregarDeshabilitado() {
        return (seleccionado == null) || !seleccionado.isDisponible();
    }

    /* ============ Agrupaciones para la vista ============ */

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

    private List<ArticuloCardDTO> filtrarPorCategoria(Categoria categoria) {
        return articulos.stream().filter(a -> a.getCategoria() == categoria).collect(Collectors.toList());
    }
    private List<ArticuloCardDTO> filtrarPorForma(List<ArticuloCardDTO> fuente, Forma forma) {
        return fuente.stream().filter(a -> a.getForma() == forma).collect(Collectors.toList());
    }
    private List<ArticuloCardDTO> filtrarPorNombreContiene(List<ArticuloCardDTO> fuente, String kw) {
        String k = kw.toLowerCase(Locale.ROOT);
        return fuente.stream().filter(a -> nombreContiene(a, k)).collect(Collectors.toList());
    }
    private boolean nombreContiene(ArticuloCardDTO a, String kw) {
        String n = Optional.ofNullable(a.getNombre()).orElse("");
        return n.toLowerCase(Locale.ROOT).contains(kw);
    }

    /* ============ Getters para la vista ============ */

    public List<ArticuloCardDTO> getMesasRedondas() { return mesasRedondas; }
    public List<ArticuloCardDTO> getMesasRectangulares() { return mesasRectangulares; }
    public List<ArticuloCardDTO> getSillasGarden() { return sillasGarden; }
    public List<ArticuloCardDTO> getSillasLifetime() { return sillasLifetime; }
    public List<ArticuloCardDTO> getCarpasChicas() { return carpasChicas; }
    public List<ArticuloCardDTO> getCarpasGrandes() { return carpasGrandes; }
    public List<ArticuloCardDTO> getCoolers() { return coolers; }
    public List<ArticuloCardDTO> getCalentones() { return calentones; }

    public ArticuloCardDTO getSeleccionado() { return seleccionado; }

    /** Cantidad elegida en el inputNumber */
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        // clamp: entre 1 y stock disponible
        int max = getMaxCantidad();
        if (max <= 0) {
            this.cantidad = 1;
        } else {
            this.cantidad = Math.max(1, Math.min(cantidad, max));
        }
    }

    /** Máximo seleccionable según stock del artículo */
    public int getMaxCantidad() {
        return (seleccionado != null) ? Math.max(0, seleccionado.getUnidades()) : 0;
    }
}
