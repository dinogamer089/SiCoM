package ui;

import helper.ArticuloHelper;
import helper.RentaHelper;
import helper.EmpleadoHelper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;
import org.primefaces.PrimeFaces;
import mx.desarollo.entity.Empleado;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Named("rentaUI")
@ViewScoped
public class RentaBeanUI implements Serializable {

    private RentaHelper rentaHelper;
    private EmpleadoHelper empleadoHelper;
    private List<Renta> rentas;
    private Renta nuevaRenta;
    private Renta rentaSeleccionada;
    private Integer idRentaSeleccionada;
    private List<String> listaEstadosRenta;
    private String estadoSiguiente;

    private List<Empleado> listaEmpleados;
    private Integer idEmpleadoSeleccionado;

    private List<Articulo> listaArticulosDisponibles;
    private ArticuloHelper articuloHelper;

    private static final List<String> ESTADOS_ORDENADOS = new ArrayList<>();
    static {
        ESTADOS_ORDENADOS.add("Aprobada");
        ESTADOS_ORDENADOS.add("Confirmado");
        ESTADOS_ORDENADOS.add("Pendiente a reparto");
        ESTADOS_ORDENADOS.add("En reparto");
        ESTADOS_ORDENADOS.add("Entregado");
        ESTADOS_ORDENADOS.add("Pendiente a recoleccion");
        ESTADOS_ORDENADOS.add("En recoleccion");
        ESTADOS_ORDENADOS.add("Finalizada");
        ESTADOS_ORDENADOS.add("Cancelada");
    }

    public RentaBeanUI() {
        rentaHelper = new RentaHelper();
        empleadoHelper = new EmpleadoHelper();
        articuloHelper = new ArticuloHelper();
    }

    @PostConstruct
    public void init() {
        nuevaRenta = new Renta();
        listaEstadosRenta = new ArrayList<>();
    }

    public void cargarRentaSeleccionada() {
        if (idRentaSeleccionada != null) {
            this.rentaSeleccionada = rentaHelper.findById(idRentaSeleccionada);
            actualizarListaEstadosPosibles();
        }
    }

    public void actualizarListaEstadosPosibles() {
        listaEstadosRenta = new ArrayList<>();

        if (rentaSeleccionada == null || rentaSeleccionada.getEstado() == null) {
            return;
        }

        String estadoActual = rentaSeleccionada.getEstado();
        int indiceActual = ESTADOS_ORDENADOS.indexOf(estadoActual);

        if (indiceActual == -1) {
            listaEstadosRenta.addAll(ESTADOS_ORDENADOS);
            return;
        }

        for (int i = indiceActual; i < ESTADOS_ORDENADOS.size(); i++) {
            listaEstadosRenta.add(ESTADOS_ORDENADOS.get(i));
        }
    }

    public void aprobarCotizacion(){
        if (rentaSeleccionada != null && "SOLICITADA".equals(rentaSeleccionada.getEstado())) {
            boolean exito = rentaHelper.cambiarEstado(rentaSeleccionada.getId(), "Aprobada");

            if (exito) {
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "La cotización ha sido aprobada.");
                cargarRentaSeleccionada();
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo aprobar la cotización.");
            }
        }
    }

    public boolean actualizarEstadoRenta() {
        if (rentaSeleccionada != null) {
            String nuevoEstado = rentaSeleccionada.getEstado();
            Integer idRenta = rentaSeleccionada.getId();

            try {
                if ("Entregado".equals(nuevoEstado) && rentaSeleccionada.getIdEmpleado() != null) {
                    rentaSeleccionada.setEntregado(rentaSeleccionada.getIdEmpleado().getNombre());
                    rentaSeleccionada.setIdEmpleado(null);
                }
                else if ("Finalizada".equals(nuevoEstado) && rentaSeleccionada.getIdEmpleado() != null) {
                    rentaSeleccionada.setRecogido(rentaSeleccionada.getIdEmpleado().getNombre());
                    rentaSeleccionada.setIdEmpleado(null);
                }

                rentaHelper.actualizarRenta(rentaSeleccionada);
                boolean exito = rentaHelper.cambiarEstado(idRenta, nuevoEstado);

                if (exito) {
                    mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Estado actualizado correctamente.");
                    cargarRentaSeleccionada();
                    return true;
                } else {
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Falló el procedimiento almacenado.");
                    return false;
                }

            } catch (Exception e) {
                cargarRentaSeleccionada();
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Error al guardar: " + e.getMessage());
                e.printStackTrace();
                FacesContext.getCurrentInstance().validationFailed();
                return false;
            }
        }
        return false;
    }

    public void onEstadoChange() {
        String nuevoEstado = rentaSeleccionada.getEstado();

        if ("En reparto".equals(nuevoEstado) || "En recoleccion".equals(nuevoEstado)) {
            this.estadoSiguiente = nuevoEstado;
            this.idEmpleadoSeleccionado = null;

            this.listaEmpleados = empleadoHelper.getAllEmpleados();

            if (listaEmpleados == null || listaEmpleados.isEmpty()) {
                mostrarMensaje(FacesMessage.SEVERITY_WARN, "Aviso", "No hay empleados en este momento.");
                cargarRentaSeleccionada();
                return;
            }

            PrimeFaces.current().executeScript("PF('dialogAsignarEmpleado').show();");
        } else {
            actualizarEstadoRenta();
        }
    }

    public void asignarEmpleadoYActualizarEstado() {
        if (idEmpleadoSeleccionado == null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un empleado.");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }

        try {
            Empleado empleado = empleadoHelper.findById(idEmpleadoSeleccionado);

            if (empleado != null) {
                String estadoOriginal = rentaSeleccionada.getEstado();
                Empleado empleadoOriginal = rentaSeleccionada.getIdEmpleado();

                rentaSeleccionada.setEstado(this.estadoSiguiente);
                rentaSeleccionada.setIdEmpleado(empleado);

                if (actualizarEstadoRenta()) {
                    this.idEmpleadoSeleccionado = null;
                    PrimeFaces.current().executeScript("PF('dialogAsignarEmpleado').hide();");
                } else {
                    rentaSeleccionada.setEstado(estadoOriginal);
                    rentaSeleccionada.setIdEmpleado(empleadoOriginal);
                }
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Empleado no encontrado.");
            }
        } catch (Exception e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Error inesperado: " + e.getMessage());
        }
    }

    public void cancelarAsignacion() {
        this.idEmpleadoSeleccionado = null;
        cargarRentaSeleccionada();
    }

    public void guardarModificacion() {
        try {
            if (rentaSeleccionada != null) {
                rentaHelper.actualizarRenta(rentaSeleccionada);


                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Datos de la renta actualizados.");
                cargarRentaSeleccionada();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron guardar los cambios: " + e.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public void cancelarModificacion() {
        cargarRentaSeleccionada();
    }

    public void obtenerTodasLasCotizaciones() {
        rentas = rentaHelper.obtenerTodasCotizaciones();
    }

    public void obtenerTodasLasRentas() {
        rentas = rentaHelper.obtenerTodasRentas();
    }

    public void cargarArticulosDisponibles() {
        this.listaArticulosDisponibles = articuloHelper.obtenerTodas();
    }

    public void recalcularTotal() {
        if (rentaSeleccionada == null || rentaSeleccionada.getDetallesRenta() == null) return;

        BigDecimal total = BigDecimal.ZERO;
        for (Detallerenta det : rentaSeleccionada.getDetallesRenta()) {
            BigDecimal precio = det.getIdarticulo().getPrecio();
            BigDecimal cantidad = new BigDecimal(det.getCantidad());
            BigDecimal subtotal = precio.multiply(cantidad);

            det.setPrecioTotal(subtotal);
            det.setPrecioUnitario(precio);

            total = total.add(subtotal);
        }
        rentaSeleccionada.setTotal(total);
    }

    public void agregarArticulo(Articulo articulo) {
        if (rentaSeleccionada == null) return;

        boolean existe = false;
        for (Detallerenta det : rentaSeleccionada.getDetallesRenta()) {
            if (det.getIdarticulo().getId().equals(articulo.getId())) {
                det.setCantidad(det.getCantidad() + 1);
                existe = true;
                break;
            }
        }

        if (!existe) {
            Detallerenta nuevoDetalle = new Detallerenta();
            nuevoDetalle.setIdrenta(rentaSeleccionada);
            nuevoDetalle.setIdarticulo(articulo);
            nuevoDetalle.setCantidad(1);
            nuevoDetalle.setPrecioUnitario(articulo.getPrecio());
            nuevoDetalle.setPrecioTotal(articulo.getPrecio()); // 1 * precio

            rentaSeleccionada.getDetallesRenta().add(nuevoDetalle);
        }

        recalcularTotal();
        mostrarMensaje(FacesMessage.SEVERITY_INFO, "Agregado", articulo.getNombre() + " agregado a la lista temporal.");
    }

    public void quitarDetalle(Detallerenta detalle) {
        rentaSeleccionada.getDetallesRenta().remove(detalle);
        recalcularTotal();
    }

    public void guardarCambiosArticulos() {
        try {
            rentaHelper.actualizarRenta(rentaSeleccionada);

            cargarRentaSeleccionada();
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Lista de artículos actualizada correctamente.");

            PrimeFaces.current().executeScript("PF('dlgModificarArticulos').hide();");
            PrimeFaces.current().ajax().update("formDetalle"); // Refrescar vista principal

        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar: " + e.getMessage());
        }
    }

    public List<Articulo> getListaArticulosDisponibles() {
        return listaArticulosDisponibles;
    }

    public void setListaArticulosDisponibles(List<Articulo> listaArticulosDisponibles) {
        this.listaArticulosDisponibles = listaArticulosDisponibles;
    }

    public String seleccionarRenta(Renta renta) {
        this.rentaSeleccionada = renta;
        return "DetalleRenta.xhtml?idRenta=" + renta.getId() + "&faces-redirect=true";
    }

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, resumen, detalle));
    }

    public List<Renta> getRentas() {
        return rentas;
    }

    public void setRentas(List<Renta> rentas) {
        this.rentas = rentas;
    }

    public Renta getNuevaRenta() {
        return nuevaRenta;
    }

    public void setNuevaRenta(Renta nuevaRenta) {
        this.nuevaRenta = nuevaRenta;
    }

    public Renta getRentaSeleccionada() {
        return rentaSeleccionada;
    }

    public void setRentaSeleccionada(Renta rentaSeleccionada) {
        this.rentaSeleccionada = rentaSeleccionada;
    }

    public Integer getIdRentaSeleccionada() {
        return idRentaSeleccionada;
    }

    public void setIdRentaSeleccionada(Integer idRentaSeleccionada) {
        this.idRentaSeleccionada = idRentaSeleccionada;
    }

    public List<String> getListaEstadosRenta() {
        return listaEstadosRenta;
    }

    public List<Empleado> getListaEmpleados() {
        return listaEmpleados;
    }

    public void setListaEmpleados(List<Empleado> listaEmpleados) {
        this.listaEmpleados = listaEmpleados;
    }

    public Integer getIdEmpleadoSeleccionado() {
        return idEmpleadoSeleccionado;
    }

    public void setIdEmpleadoSeleccionado(Integer idEmpleadoSeleccionado) {
        this.idEmpleadoSeleccionado = idEmpleadoSeleccionado;
    }
}