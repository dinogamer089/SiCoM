package ui;

import helper.TarjetaAlmacenHelper;
import helper.dto.TarjetaAlmacenDTO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.MovimientoAlmacen;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Managed Bean para la gestion de tarjetas de almacen.
 * Permite consultar movimientos por articulo y fecha.
 */
@Named("tarjetaAlmacenUI")
@SessionScoped
public class TarjetaAlmacenBeanUI implements Serializable {

    private List<Articulo> articulos;
    private Integer articuloSeleccionadoId;
    private LocalDate fechaConsulta;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private List<TarjetaAlmacenDTO> tarjetaDelDia;
    private String nombreArticuloSeleccionado;

    // Datos para consulta por rango
    private boolean consultaPorRango = false;

    /**
     * Inicializacion del bean.
     */
    @PostConstruct
    public void init() {
        cargarArticulos();
        fechaConsulta = LocalDate.now();
        fechaInicio = LocalDate.now().minusDays(7);
        fechaFin = LocalDate.now();
        tarjetaDelDia = new ArrayList<>();
    }

    /**
     * Carga la lista de articulos disponibles.
     */
    public void cargarArticulos() {
        try {
            articulos = ServiceFacadeLocator.getInstanceFacadeArticulo().obtenerArticulos();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudieron cargar los articulos: " + e.getMessage()));
        }
    }

    /**
     * Genera la tarjeta de almacen para la fecha seleccionada.
     */
    public void generarTarjetaDia() {
        if (articuloSeleccionadoId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Advertencia", "Seleccione un articulo"));
            return;
        }

        if (fechaConsulta == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Advertencia", "Seleccione una fecha"));
            return;
        }

        try {
            System.out.println("=== DEBUG: Generando tarjeta ===");
            System.out.println("Articulo ID: " + articuloSeleccionadoId);
            System.out.println("Fecha: " + fechaConsulta);

            var facade = ServiceFacadeLocator.getInstanceFacadeTarjetaAlmacen();

            // Obtener nombre del articulo
            Articulo articulo = articulos.stream()
                .filter(a -> a.getId().equals(articuloSeleccionadoId))
                .findFirst()
                .orElse(null);

            if (articulo == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Articulo no encontrado"));
                return;
            }

            nombreArticuloSeleccionado = articulo.getNombre();
            System.out.println("Articulo: " + nombreArticuloSeleccionado);

            // Obtener inventario inicial
            int inventarioInicial = facade.obtenerInventarioInicial(articuloSeleccionadoId, fechaConsulta);
            System.out.println("Inventario Inicial: " + inventarioInicial);

            // Obtener movimientos del dia
            List<MovimientoAlmacen> movimientos = facade.consultarMovimientosPorFecha(
                articuloSeleccionadoId, fechaConsulta);
            System.out.println("Movimientos encontrados: " + (movimientos != null ? movimientos.size() : 0));

            // Generar tarjeta
            tarjetaDelDia = new ArrayList<>();

            // Agregar movimientos
            if (movimientos != null && !movimientos.isEmpty()) {
                List<TarjetaAlmacenDTO> filas = TarjetaAlmacenHelper.generarTarjeta(
                    movimientos, inventarioInicial);
                tarjetaDelDia.addAll(filas);
                System.out.println("Filas agregadas: " + filas.size());
            }

            System.out.println("Total filas en tarjeta: " + tarjetaDelDia.size());

            if (tarjetaDelDia.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Informacion", "No hay movimientos para esta fecha"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Exito", "Tarjeta generada con " + tarjetaDelDia.size() + " registros"));
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Error al generar tarjeta: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * Genera la tarjeta de almacen por rango de fechas.
     */
    public void generarTarjetaRango() {
        if (articuloSeleccionadoId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Advertencia", "Seleccione un articulo"));
            return;
        }

        if (fechaInicio == null || fechaFin == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Advertencia", "Seleccione ambas fechas"));
            return;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Advertencia", "La fecha inicial debe ser anterior a la final"));
            return;
        }

        try {
            var facade = ServiceFacadeLocator.getInstanceFacadeTarjetaAlmacen();

            // Obtener nombre del articulo
            Articulo articulo = articulos.stream()
                .filter(a -> a.getId().equals(articuloSeleccionadoId))
                .findFirst()
                .orElse(null);

            if (articulo == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Articulo no encontrado"));
                return;
            }

            nombreArticuloSeleccionado = articulo.getNombre();

            // Obtener inventario inicial de la primera fecha
            int inventarioInicial = facade.obtenerInventarioInicial(articuloSeleccionadoId, fechaInicio);

            // Obtener movimientos del rango
            List<MovimientoAlmacen> movimientos = facade.consultarMovimientosPorRango(
                articuloSeleccionadoId, fechaInicio, fechaFin);

            // Generar tarjeta
            tarjetaDelDia = new ArrayList<>();

            // Agregar movimientos
            if (movimientos != null && !movimientos.isEmpty()) {
                List<TarjetaAlmacenDTO> filas = TarjetaAlmacenHelper.generarTarjeta(
                    movimientos, inventarioInicial);
                tarjetaDelDia.addAll(filas);
            }

            if (tarjetaDelDia.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Informacion", "No hay movimientos en este rango"));
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Error al generar tarjeta: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * Limpia la consulta actual.
     */
    public void limpiarConsulta() {
        articuloSeleccionadoId = null;
        nombreArticuloSeleccionado = null;
        tarjetaDelDia = new ArrayList<>();
        fechaConsulta = LocalDate.now();
    }

    /**
     * Activa el modo de consulta por dia.
     */
    public void activarConsultaPorDia() {
        this.consultaPorRango = false;
    }

    /**
     * Activa el modo de consulta por rango.
     */
    public void activarConsultaPorRango() {
        this.consultaPorRango = true;
    }

    // Getters y Setters
    public List<Articulo> getArticulos() {
        // Siempre obtener la lista fresca para que se actualice cuando se agregan nuevos articulos
        cargarArticulos();
        return articulos;
    }

    public void setArticulos(List<Articulo> articulos) {
        this.articulos = articulos;
    }

    public Integer getArticuloSeleccionadoId() {
        return articuloSeleccionadoId;
    }

    public void setArticuloSeleccionadoId(Integer articuloSeleccionadoId) {
        this.articuloSeleccionadoId = articuloSeleccionadoId;
    }

    public LocalDate getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDate fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public List<TarjetaAlmacenDTO> getTarjetaDelDia() {
        return tarjetaDelDia;
    }

    public void setTarjetaDelDia(List<TarjetaAlmacenDTO> tarjetaDelDia) {
        this.tarjetaDelDia = tarjetaDelDia;
    }

    public String getNombreArticuloSeleccionado() {
        return nombreArticuloSeleccionado;
    }

    public void setNombreArticuloSeleccionado(String nombreArticuloSeleccionado) {
        this.nombreArticuloSeleccionado = nombreArticuloSeleccionado;
    }

    public boolean isConsultaPorRango() {
        return consultaPorRango;
    }

    public void setConsultaPorRango(boolean consultaPorRango) {
        this.consultaPorRango = consultaPorRango;
    }
}
