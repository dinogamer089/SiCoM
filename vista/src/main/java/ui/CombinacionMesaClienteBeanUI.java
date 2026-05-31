package ui;

import helper.CombinacionMesaHelper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.CombinacionMesa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named("combinacionMesaClienteUI")
@ViewScoped
public class CombinacionMesaClienteBeanUI implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CombinacionMesaHelper helper = new CombinacionMesaHelper();

    // Selecciones actuales
    private Integer mesaId;
    private Integer mantelId;
    private Integer caminoId;
    private Integer cubreId;

    // Combinaciones filtradas para la mesa actual
    private List<CombinacionMesa> combinacionesMesa = new ArrayList<>();

    // Opciones para la UI
    private List<Articulo> mantelesDisponibles = new ArrayList<>();
    private List<Articulo> caminosDisponibles = new ArrayList<>();
    private List<Articulo> cubresDisponibles = new ArrayList<>();

    // Combinación actualmente encontrada (si existe)
    private CombinacionMesa combinacionSeleccionada;

    // === MESA SELECCIONADA DESDE LA IZQUIERDA (remoteCommand) ===

    /**
     * Metodo que se ejecuta cuando el cliente selecciona una mesa desde el catalogo (remoteCommand).
     * Toma el ID de la mesa desde los parametros de la peticion, carga todas las combinaciones,
     * filtra por mesa y genera la lista de manteles disponibles para esa mesa.
     * @Throws Si el ID de mesa no es valido o la base de datos rechaza la consulta de combinaciones.
     */
    public void onMesaSeleccion() {
        try {
            var ctx = FacesContext.getCurrentInstance();
            var params = ctx.getExternalContext().getRequestParameterMap();
            String sid = params.get("mesaId");

            if (sid == null || sid.isBlank()) {
                limpiar();
                return;
            }

            mesaId = Integer.valueOf(sid);
            System.out.println("[cliente] Mesa seleccionada para textiles, id=" + mesaId);

            // Reiniciar selección de textiles
            mantelId = null;
            caminoId = null;
            cubreId = null;
            combinacionSeleccionada = null;

            // Cargar todas las combinaciones y filtrar por mesa
            List<CombinacionMesa> todas = helper.obtenerTodas();
            combinacionesMesa = new ArrayList<>();

            if (todas != null) {
                for (CombinacionMesa c : todas) {
                    if (c == null || c.getMesa() == null || c.getMesa().getId() == null) {
                        continue;
                    }
                    if (c.getMesa().getId().equals(mesaId)) {
                        combinacionesMesa.add(c);
                    }
                }
            }

            // Construir lista de manteles distintos para esa mesa
            Map<Integer, Articulo> mapManteles = new LinkedHashMap<>();
            for (CombinacionMesa c : combinacionesMesa) {
                if (c.getMantel() != null && c.getMantel().getId() != null) {
                    mapManteles.putIfAbsent(c.getMantel().getId(), c.getMantel());
                }
            }
            mantelesDisponibles = new ArrayList<>(mapManteles.values());

            // Extras se calculan cuando se elige mantel
            caminosDisponibles = new ArrayList<>();
            cubresDisponibles = new ArrayList<>();

        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            limpiar();
        }
    }

    // === CAMBIO DE MANTEL ===

    /**
     * Metodo que se ejecuta cuando cambia el mantel seleccionado por el cliente.
     * Reinicia extras (camino y cubre), recalcula las listas de caminos/cubres disponibles
     * para la combinacion de mesa + mantel, y actualiza la combinacion seleccionada.
     */
    public void onMantelChange() {
        // Reinicia extras
        caminoId = null;
        cubreId = null;
        caminosDisponibles = new ArrayList<>();
        cubresDisponibles = new ArrayList<>();

        if (mesaId == null || mantelId == null || combinacionesMesa == null) {
            actualizarCombinacionSeleccionada();
            return;
        }

        // Extras disponibles SOLO para esa mesa + mantel
        Map<Integer, Articulo> mapCaminos = new LinkedHashMap<>();
        Map<Integer, Articulo> mapCubres = new LinkedHashMap<>();

        for (CombinacionMesa c : combinacionesMesa) {
            if (c.getMantel() == null || c.getMantel().getId() == null) {
                continue;
            }
            if (!c.getMantel().getId().equals(mantelId)) {
                continue;
            }

            if (c.getCamino() != null && c.getCamino().getId() != null) {
                mapCaminos.putIfAbsent(c.getCamino().getId(), c.getCamino());
            }
            if (c.getCubre() != null && c.getCubre().getId() != null) {
                mapCubres.putIfAbsent(c.getCubre().getId(), c.getCubre());
            }
        }

        caminosDisponibles = new ArrayList<>(mapCaminos.values());
        cubresDisponibles = new ArrayList<>(mapCubres.values());

        actualizarCombinacionSeleccionada();
    }

    // === CAMBIO DE CAMINO ===

    /**
     * Metodo que se ejecuta cuando cambia el camino seleccionado.
     * Si se elige un camino, limpia el cubre para asegurar que solo haya un extra a la vez.
     * Luego recalcula la combinacion seleccionada.
     */
    public void onCaminoChange() {
        if (caminoId != null) {
            // Si eliges camino, limpias cubre para asegurar un solo extra
            cubreId = null;
        }
        actualizarCombinacionSeleccionada();
    }

    // === CAMBIO DE CUBRE ===

    /**
     * Metodo que se ejecuta cuando cambia el cubremantel seleccionado.
     * Si se elige un cubre, limpia el camino para asegurar que solo haya un extra a la vez.
     * Luego recalcula la combinacion seleccionada.
     */
    public void onCubreChange() {
        if (cubreId != null) {
            // Si eliges cubre, limpias camino para asegurar un solo extra
            caminoId = null;
        }
        actualizarCombinacionSeleccionada();
    }

    // === BUSCAR LA COMBINACIÓN CORRECTA ===

    /**
     * Metodo privado para actualizar la combinacionSeleccionada
     * en base a los IDs de mesa, mantel y el extra (camino o cubre) elegidos.
     * Solo considera combinaciones que cumplan exactamente la seleccion actual.
     */
    private void actualizarCombinacionSeleccionada() {
        combinacionSeleccionada = null;

        if (mesaId == null || mantelId == null || combinacionesMesa == null) {
            return;
        }

        for (CombinacionMesa c : combinacionesMesa) {
            if (c == null || c.getMesa() == null || c.getMantel() == null) {
                continue;
            }
            if (c.getMesa().getId() == null || c.getMantel().getId() == null) {
                continue;
            }
            if (!c.getMesa().getId().equals(mesaId)) continue;
            if (!c.getMantel().getId().equals(mantelId)) continue;

            boolean okExtra;
            if (caminoId != null) {
                okExtra = (c.getCamino() != null &&
                        c.getCamino().getId() != null &&
                        c.getCamino().getId().equals(caminoId) &&
                        c.getCubre() == null);
            } else if (cubreId != null) {
                okExtra = (c.getCubre() != null &&
                        c.getCubre().getId() != null &&
                        c.getCubre().getId().equals(cubreId) &&
                        c.getCamino() == null);
            } else {
                // Sin extra: la combinación debe no tener ni camino ni cubre
                okExtra = (c.getCamino() == null && c.getCubre() == null);
            }

            if (okExtra) {
                combinacionSeleccionada = c;
                System.out.println("[cliente] Combinación encontrada id=" + c.getId());
                break;
            }
        }
    }

    // === LIMPIAR TODO (por si se usa después) ===

    /**
     * Metodo privado para limpiar todo el estado del bean de combinaciones de cliente.
     * Reinicia IDs, listas de opciones y la combinacion seleccionada.
     */
    private void limpiar() {
        mesaId = null;
        mantelId = null;
        caminoId = null;
        cubreId = null;
        combinacionesMesa = new ArrayList<>();
        mantelesDisponibles = new ArrayList<>();
        caminosDisponibles = new ArrayList<>();
        cubresDisponibles = new ArrayList<>();
        combinacionSeleccionada = null;
    }

    // === GETTERS PARA LA VISTA ===

    /**
     * Metodo getter para obtener el ID de la mesa seleccionada.
     * @return Objeto Integer con el ID de la mesa o null.
     */
    public Integer getMesaId() {
        return mesaId;
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
     * Metodo getter para obtener el ID del cubre seleccionado.
     * @return Objeto Integer con el ID del cubre o null.
     */
    public Integer getCubreId() {
        return cubreId;
    }

    /**
     * Metodo setter para asignar el ID del cubre seleccionado.
     * @Params Objeto de tipo Integer cubreId
     */
    public void setCubreId(Integer cubreId) {
        this.cubreId = cubreId;
    }

    /**
     * Metodo getter para obtener la lista de manteles disponibles para la mesa seleccionada.
     * @return Lista de objetos Articulo usados como manteles.
     */
    public List<Articulo> getMantelesDisponibles() {
        return mantelesDisponibles;
    }

    /**
     * Metodo getter para obtener la lista de caminos disponibles para la mesa/mantel seleccionados.
     * @return Lista de objetos Articulo usados como caminos.
     */
    public List<Articulo> getCaminosDisponibles() {
        return caminosDisponibles;
    }

    /**
     * Metodo getter para obtener la lista de cubres disponibles para la mesa/mantel seleccionados.
     * @return Lista de objetos Articulo usados como cubremanteles.
     */
    public List<Articulo> getCubresDisponibles() {
        return cubresDisponibles;
    }

    /**
     * Metodo getter para obtener la combinacion actualmente seleccionada.
     * @return Objeto CombinacionMesa encontrado o null si no hay coincidencia.
     */
    public CombinacionMesa getCombinacionSeleccionada() {
        return combinacionSeleccionada;
    }

    /**
     * Imagen que usa el panel de Vista previa.
     * Si no hay combinación seleccionada, devuelve null y el xhtml
     * se encarga de mostrar la imagen normal del artículo.
     * @return Cadena con el Data URL de la imagen de combinacion o null si no hay seleccion.
     */
    public String getImagenPreview() {
        if (combinacionSeleccionada == null) {
            return null;
        }
        try {
            return helper.toDataUrl(combinacionSeleccionada);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
