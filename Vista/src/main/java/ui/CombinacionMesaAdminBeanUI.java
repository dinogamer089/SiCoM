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

    /** Id de la combinación seleccionada  */
    private Integer selId;

    @PostConstruct
    public void init() {
        refrescarLista();
        cargarMesasDisponibles();
        mantelesDisponibles = new ArrayList<>();
        caminosDisponibles = new ArrayList<>();
        cubresDisponibles = new ArrayList<>();
    }

    private void refrescarLista() {
        combinaciones = helper.obtenerTodas();
    }

    private void cargarMesasDisponibles() {
        FacadeArticulo f = ServiceFacadeLocator.getInstanceFacadeArticulo();
        List<Articulo> todos = f.obtenerArticulos();
        mesasDisponibles = todos.stream()
                .filter(a -> a.getCategoria() == Categoria.MESA)
                .collect(Collectors.toList());
    }

    // ==== Textiles según mesa ====

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

    private boolean formaCompatible(Articulo a, Forma formaMesa) {
        if (a == null || formaMesa == null || a.getForma() == null) return false;
        return a.getForma() == formaMesa || a.getForma() == Forma.UNIVERSAL;
    }

    public void onMantelChange() {
        caminoId = null;
        cubreId = null;
    }

    public void onCaminoChange() {
        // Si selecciona un camino, limpiamos cubre para mantener exclusividad
        if (caminoId != null) {
            cubreId = null;
        }
    }

    public void onCubreChange() {
        // Si selecciona un cubre, limpiamos camino para mantener exclusividad
        if (cubreId != null) {
            caminoId = null;
        }
    }

    // ==== Upload de imagen ====

    public void handleFileUpload(FileUploadEvent event) throws IOException {
        UploadedFile file = event.getFile();
        if (file != null) {
            imagenBytes = file.getContent();
            imagenMime = file.getContentType();
        }
    }

    // ==== Guardar combinación ====

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

    /** Usado desde el botón Cancelar del diálogo para limpiar campos. */
    public void cancelarAlta() {
        limpiarFormulario();
    }

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

    public void limpiarSeleccion() {
        seleccionada = null;
        selId = null;
    }

    // ==== Getters / Setters para la vista ====

    public List<CombinacionMesa> getCombinaciones() {
        return combinaciones;
    }

    public CombinacionMesa getSeleccionada() {
        return seleccionada;
    }

    public void setSeleccionada(CombinacionMesa seleccionada) {
        this.seleccionada = seleccionada;
    }

    public Integer getMesaId() {
        return mesaId;
    }

    public void setMesaId(Integer mesaId) {
        this.mesaId = mesaId;
    }

    public Integer getMantelId() {
        return mantelId;
    }

    public void setMantelId(Integer mantelId) {
        this.mantelId = mantelId;
    }

    public Integer getCaminoId() {
        return caminoId;
    }

    public void setCaminoId(Integer caminoId) {
        this.caminoId = caminoId;
    }

    public Integer getCubreId() {
        return cubreId;
    }

    public void setCubreId(Integer cubreId) {
        this.cubreId = cubreId;
    }

    public List<Articulo> getMesasDisponibles() {
        return mesasDisponibles;
    }

    public List<Articulo> getMantelesDisponibles() {
        return mantelesDisponibles;
    }

    public List<Articulo> getCaminosDisponibles() {
        return caminosDisponibles;
    }

    public List<Articulo> getCubresDisponibles() {
        return cubresDisponibles;
    }

    public Integer getSelId() {
        return selId;
    }

    public void setSelId(Integer selId) {
        this.selId = selId;
    }

    public String getMiniatura(CombinacionMesa c) {
        return helper.toDataUrl(c);
    }

    /** Alias usado en miniatura(c). */
    public String miniatura(CombinacionMesa c) {
        return getMiniatura(c);
    }

    // ==== Utilerías internas ====

    private Optional<Articulo> resolveArticulo(Integer id) {
        if (id == null) return Optional.empty();
        return ServiceFacadeLocator.getInstanceFacadeArticulo().obtenerArticuloPorId(id);
    }

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
