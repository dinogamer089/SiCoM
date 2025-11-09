package ui;

import helper.RentaHelper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Renta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("rentaUI")
@ViewScoped
public class RentaBeanUI implements Serializable {

    private RentaHelper rentaHelper;
    private List<Renta> rentas;
    private Renta nuevaRenta;
    private Renta rentaSeleccionada;
    private Integer idRentaSeleccionada;
    private List<String> listaEstadosRenta;

    public RentaBeanUI() {
        rentaHelper = new RentaHelper();
    }

    @PostConstruct
    public void init() {
        nuevaRenta = new Renta();

        listaEstadosRenta = new ArrayList<>();
        listaEstadosRenta.add("Aprobado");
        listaEstadosRenta.add("Confirmado");
        listaEstadosRenta.add("Pendiente a reparto");
        listaEstadosRenta.add("En reparto");
        listaEstadosRenta.add("Entregado");
        listaEstadosRenta.add("Pendiente a recoleccion");
        listaEstadosRenta.add("En recoleccion");
        listaEstadosRenta.add("Finalizada");
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
        if (rentaSeleccionada != null && "Pendiente por aprobar".equals(rentaSeleccionada.getEstado())) {
            try {
                rentaSeleccionada.setEstado("Aprobado");

                rentaHelper.actualizarRenta(rentaSeleccionada);

                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "La cotización ha sido aprobada.");

                cargarRentaSeleccionada();

            } catch (Exception e) {
                rentaSeleccionada.setEstado("Pendiente por aprobar");
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo aprobar la cotización: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void actualizarEstadoRenta() {
        if (rentaSeleccionada != null) {
            try {
                rentaHelper.actualizarRenta(rentaSeleccionada);
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Estado actualizado a: " + rentaSeleccionada.getEstado());
            } catch (Exception e) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el estado: " + e.getMessage());
                e.printStackTrace();

                cargarRentaSeleccionada();
            }
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

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, resumen, detalle));
    }
}