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

    public RentaBeanUI() {
        rentaHelper = new RentaHelper();
        empleadoHelper = new EmpleadoHelper();
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
            try {
                if ("Entregado".equals(nuevoEstado)) {
                    rentaSeleccionada.setEntregado(rentaSeleccionada.getIdEmpleado().getNombre());
                    rentaSeleccionada.setIdEmpleado(null);
                }
                else if ("Finalizada".equals(nuevoEstado)) {
                    rentaSeleccionada.setRecogido(rentaSeleccionada.getIdEmpleado().getNombre());
                    rentaSeleccionada.setIdEmpleado(null);
                }

                rentaHelper.actualizarRenta(rentaSeleccionada);
                cargarRentaSeleccionada();
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Estado actualizado a: " + rentaSeleccionada.getEstado());
            } catch (Exception e) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el estado: " + e.getMessage());
                e.printStackTrace();
                FacesContext.getCurrentInstance().validationFailed();
            }
        }
    }

    public void onEstadoChange() {
        String nuevoEstado = rentaSeleccionada.getEstado();

        if ("En reparto".equals(nuevoEstado) || "En recoleccion".equals(nuevoEstado)) {
            this.estadoSiguiente = nuevoEstado;
            this.idEmpleadoInput = null;
            PrimeFaces.current().executeScript("PF('dialogAsignarEmpleado').show();");
        } else {
            actualizarEstadoRenta();
        }
    }

    public void asignarEmpleadoYActualizarEstado() {
        System.out.println("--- MÉTODO 'asignarEmpleadoYActualizarEstado' SÍ FUE LLAMADO ---");
        if (idEmpleadoInput == null || idEmpleadoInput.trim().isEmpty()) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Campo requerido", "Debe ingresar un ID de empleado.");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }

        try {
            Integer empleadoId = Integer.parseInt(idEmpleadoInput);
            Empleado empleado = empleadoHelper.findById(empleadoId);
            System.out.println(empleado.getId());

            if (empleado != null) {
                rentaSeleccionada.setEstado(this.estadoSiguiente);
                rentaSeleccionada.setIdEmpleado(empleado);

                actualizarEstadoRenta();

            } else {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Empleado con ID " + empleadoId + " no encontrado.");
                FacesContext.getCurrentInstance().validationFailed();
            }
        } catch (NumberFormatException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "El ID debe ser un número.");
            FacesContext.getCurrentInstance().validationFailed();
        } catch (Exception e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "Error al buscar empleado: " + e.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
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

    public String getIdEmpleadoInput() {
        return idEmpleadoInput;
    }

    public void setIdEmpleadoInput(String idEmpleadoInput) {
        this.idEmpleadoInput = idEmpleadoInput;
    }

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, resumen, detalle));
    }
}