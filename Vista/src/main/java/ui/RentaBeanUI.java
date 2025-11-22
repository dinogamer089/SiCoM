package ui;

import helper.RentaHelper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.desarollo.entity.Renta;

import java.io.Serializable;
import java.util.List;

@Named("rentaUI")
@ViewScoped
public class RentaBeanUI implements Serializable {

    private RentaHelper rentaHelper;
    private List<Renta> rentas;
    private Renta nuevaRenta;
    private Renta rentaSeleccionada;
    private Integer idRentaSeleccionada;

    public RentaBeanUI() {
        rentaHelper = new RentaHelper();
    }

    @PostConstruct
    public void init() {
        nuevaRenta = new Renta();

        obtenerTodasLasCotizaciones();
    }

    public void obtenerTodasLasCotizaciones() {
        rentas = rentaHelper.obtenerTodasCotizaciones();
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
            try {
                rentaSeleccionada.setEstado("Aprobada");

                rentaHelper.actualizarRenta(rentaSeleccionada);

                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "La cotización ha sido aprobada.");

                cargarRentaSeleccionada();

            } catch (Exception e) {
                rentaSeleccionada.setEstado("SOLICITADA");
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo aprobar la cotización: " + e.getMessage());
                e.printStackTrace();
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

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, resumen, detalle));
    }
}