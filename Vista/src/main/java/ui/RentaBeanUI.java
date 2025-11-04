package ui;

import helper.RentaHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.desarollo.entity.Renta;
import mx.desarollo.entity.Cliente; // Asumimos que necesitas Cliente para la nueva Renta

import java.io.Serializable;
import java.util.List;

@Named("rentaUI")
@SessionScoped
public class RentaBeanUI implements Serializable {

    private RentaHelper rentaHelper;
    private List<Renta> rentas;
    private Renta nuevaRenta;
    private Renta rentaSeleccionada;

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

        return "DetalleRenta.xhtml?faces-redirect=true";
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

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, resumen, detalle));
    }
}