package ui;

import helper.DetalleHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.desarollo.entity.Detallerenta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("detalleUI")
@SessionScoped
public class DetalleBeanUI implements Serializable {

    private DetalleHelper detalleHelper;
    private List<Detallerenta> detalles;
    private Detallerenta nuevoDetalle;

    public DetalleBeanUI() {
        detalleHelper = new DetalleHelper();
    }

    @PostConstruct
    public void init() {
        nuevoDetalle = new Detallerenta();
        // Nota: Generalmente, los detalles se cargan y gestionan dentro del contexto de una RENTA.
        // Aquí cargamos todos para fines de inicialización.
        obtenerTodosLosDetalles();
    }

    public void obtenerTodosLosDetalles() {
        detalles = detalleHelper.obtenerTodas();
    }

    public void guardarDetalle() {
        try {
            // Validación básica (Asumiendo que Detallerenta necesita cantidad, idArticulo y idRenta)
            if (nuevoDetalle.getCantidadRentada() == null || nuevoDetalle.getCantidadRentada() <= 0) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "La cantidad de artículos debe ser mayor a 0.");
                return;
            }
            /*
            // En un flujo real, esto validaría que las FK (idRenta, idArticulo) estén establecidas
            if (nuevoDetalle.getIdRenta() == null || nuevoDetalle.getIdArticulo() == null) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Debe asociar la Renta y el Artículo.");
                return;
            }
            */

            detalleHelper.guardarDetalle(nuevoDetalle);
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Detalle de renta creado correctamente.");

            obtenerTodosLosDetalles();

            nuevoDetalle = new Detallerenta();

        } catch (Exception e) {
            System.err.println("Error al guardar detalle de renta: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el detalle de renta: " + e.getMessage());
        }
    }

    public List<Detallerenta> getDetalles() {
        return detalles;
    }

    public Detallerenta getNuevoDetalle() {
        return nuevoDetalle;
    }

    public void setNuevoDetalle(Detallerenta nuevoDetalle) {
        this.nuevoDetalle = nuevoDetalle;
    }

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, resumen, detalle));
    }
}
