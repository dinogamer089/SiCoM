package ui;

import helper.RentaHelper;
import helper.EmpleadoHelper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Renta;
import org.primefaces.PrimeFaces;
import mx.desarollo.entity.Empleado;

import java.io.Serializable;
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
    private String idEmpleadoInput;
    private String estadoSiguiente;
    private List<Empleado> listaEmpleados;
    private Integer idEmpleadoSeleccionado;

    public RentaBeanUI() {
        rentaHelper = new RentaHelper();
        empleadoHelper = new EmpleadoHelper();
    }

    @PostConstruct
    public void init() {
        nuevaRenta = new Renta();

        listaEstadosRenta = new ArrayList<>();
        listaEstadosRenta.add("Aprobada");
        listaEstadosRenta.add("Confirmado");
        listaEstadosRenta.add("Pendiente a reparto");
        listaEstadosRenta.add("En reparto");
        listaEstadosRenta.add("Entregado");
        listaEstadosRenta.add("Pendiente a recoleccion");
        listaEstadosRenta.add("En recoleccion");
        listaEstadosRenta.add("Finalizada");

        listaEmpleados = empleadoHelper.getAllEmpleados();
    }

    public void obtenerTodasLasCotizaciones() {
        rentas = rentaHelper.obtenerTodasCotizaciones();
    }

    public void obtenerTodasLasRentas() {
        rentas = rentaHelper.obtenerTodasRentas();
    }


    public String seleccionarRenta(Renta renta) {
        this.rentaSeleccionada = renta;

        return "DetalleRenta.xhtml?idRenta=" + renta.getId() + "&faces-redirect=true";
    }

    public void cargarRentaSeleccionada() {
        if (idRentaSeleccionada != null) {
            this.rentaSeleccionada = rentaHelper.findById(idRentaSeleccionada);
        }
    }

    public void aprobarCotizacion(){
        if (rentaSeleccionada != null && "SOLICITADA".equals(rentaSeleccionada.getEstado())) {

            boolean exito = rentaHelper.cambiarEstado(rentaSeleccionada.getId(), "Aprobada");

            if (exito) {
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "La cotización ha sido aprobada y el stock reservado.");
                cargarRentaSeleccionada();
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo aprobar la cotización. Verifique el log.");
            }
        }
    }

    public void actualizarEstadoRenta() {
        if (rentaSeleccionada != null) {
            String nuevoEstado = rentaSeleccionada.getEstado();
            Integer idRenta = rentaSeleccionada.getId();

            try {
                if ("Entregado".equals(nuevoEstado)) {
                    if (rentaSeleccionada.getIdEmpleado() != null) {
                        rentaSeleccionada.setEntregado(rentaSeleccionada.getIdEmpleado().getNombre());
                        rentaSeleccionada.setIdEmpleado(null);
                    }
                }
                else if ("Finalizada".equals(nuevoEstado)) {
                    if (rentaSeleccionada.getIdEmpleado() != null) {
                        rentaSeleccionada.setRecogido(rentaSeleccionada.getIdEmpleado().getNombre());
                        rentaSeleccionada.setIdEmpleado(null);
                    }
                }

                rentaHelper.actualizarRenta(rentaSeleccionada);

                boolean exito = rentaHelper.cambiarEstado(idRenta, nuevoEstado);

                if (exito) {
                    mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Estado actualizado correctamente.");
                    cargarRentaSeleccionada();
                } else {
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Falló el procedimiento almacenado.");
                }

            } catch (Exception e) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar: " + e.getMessage());
                e.printStackTrace();
                FacesContext.getCurrentInstance().validationFailed();
            }
        }
    }

    public void onEstadoChange() {
        String nuevoEstado = rentaSeleccionada.getEstado();

        if ("En reparto".equals(nuevoEstado) || "En recoleccion".equals(nuevoEstado)) {
            this.estadoSiguiente = nuevoEstado;
            this.idEmpleadoSeleccionado = null;
            PrimeFaces.current().executeScript("PF('dialogAsignarEmpleado').show();");
        } else {
            actualizarEstadoRenta();
        }
    }

    public void asignarEmpleadoYActualizarEstado() {
        System.out.println("--- Asignando empleado ---");

        if (idEmpleadoSeleccionado == null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un empleado.");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }

        try {
            Empleado empleado = empleadoHelper.findById(idEmpleadoSeleccionado);

            if (empleado != null) {
                rentaSeleccionada.setEstado(this.estadoSiguiente);
                rentaSeleccionada.setIdEmpleado(empleado);

                actualizarEstadoRenta();

                this.idEmpleadoSeleccionado = null; // Limpiar
                PrimeFaces.current().executeScript("PF('dialogAsignarEmpleado').hide();");

            } else {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Empleado no encontrado.");
            }
        } catch (Exception e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Error inesperado: " + e.getMessage());
        }
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

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, resumen, detalle));
    }
}