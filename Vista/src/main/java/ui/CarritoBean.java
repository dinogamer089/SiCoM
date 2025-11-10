package ui;

import helper.Carrito;
import helper.CarritoItem;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Cliente;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.integration.ServiceFacadeLocator;
import mx.desarollo.facade.FacadeRenta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("carritoBean")
@SessionScoped
public class CarritoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Carrito carrito;
    private boolean aceptaTerminos;

    private List<Articulo> catalogoSimulado;


    private String nombreCliente;
    private String telefono1;
    private String direccionEntrega;
    private Date fechaEntrega;


    private FacadeRenta facadeRenta;

    @PostConstruct
    public void init() {
        carrito = new Carrito();
        aceptaTerminos = false;
        inicializarCatalogoSimulado();
        precargarArticulosDeEjemplo();

        facadeRenta = ServiceFacadeLocator.getInstanceFacadeRenta();
    }

    /**
     * Carga el catálogo "simulado"
     * Solo se agregan los artículos con id 2, 4 y 3.
     */
    private void inicializarCatalogoSimulado() {
        catalogoSimulado = new ArrayList<>();

        try {
            java.util.List<Articulo> todos =
                    ServiceFacadeLocator.getInstanceFacadeArticulo().obtenerArticulos();

            if (todos == null || todos.isEmpty()) {
                System.out.println("No se encontraron artículos en la BD para el catálogo simulado.");
                return;
            }

            int[] idsDeseados = {2, 4, 3};

            for (int idBuscado : idsDeseados) {
                Integer idObj = idBuscado;
                for (Articulo art : todos) {
                    if (art != null
                            && art.getIdarticulo() != null
                            && idObj.equals(art.getIdarticulo())) {


                        catalogoSimulado.add(art);
                        System.out.println("Artículo agregado al catálogo simulado desde BD: "
                                + art.getIdarticulo() + " - " + art.getNombre()
                                + " (stock=" + art.getCantidad() + ")");
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error al inicializar catálogo simulado desde BD:");
            e.printStackTrace();
        }
    }


    private void precargarArticulosDeEjemplo() {
        if (catalogoSimulado == null || catalogoSimulado.isEmpty()) {
            System.out.println("Catálogo simulado vacío, no se precargan artículos en el carrito.");
            return;
        }

        agregarArticuloPorId(2);
        agregarArticuloPorId(4);
        agregarArticuloPorId(3);
    }

    private void agregarArticuloPorId(int idArticulo) {
        if (catalogoSimulado == null) return;

        Integer idObj = idArticulo;
        for (Articulo art : catalogoSimulado) {
            if (art != null
                    && art.getIdarticulo() != null
                    && idObj.equals(art.getIdarticulo())) {

                boolean added = carrito.agregarArticulo(art);
                if (added) {
                    System.out.println("Artículo id=" + idArticulo
                            + " agregado al carrito (stock=" + art.getCantidad() + ").");
                } else {
                    System.out.println("No se pudo agregar al carrito el artículo id=" + idArticulo
                            + " (posible falta de stock).");
                }
                break;
            }
        }
    }


    public void agregarArticulo(Articulo articulo) {
        if (articulo == null) return;
        boolean added = carrito.agregarArticulo(articulo);
        if (!added) {
            System.out.println("No se pudo agregar el articulo (posible falta de stock): " + articulo.getNombre());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Stock insuficiente",
                    "No hay suficiente stock para: " + articulo.getNombre()
            ));
        }
    }

    public void eliminarItem(CarritoItem item) {
        carrito.eliminarItem(item);
    }

    public void incrementarCantidad(CarritoItem item) {
        carrito.incrementarCantidad(item);
    }

    public void decrementarCantidad(CarritoItem item) {
        carrito.decrementarCantidad(item);
    }

    public void vaciarCarrito() {
        carrito.vaciar();
    }


    public void confirmarCotizacion() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            if (carrito.getItems().isEmpty()) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Aviso",
                        "El carrito está vacío."
                ));
                ctx.validationFailed();
                return;
            }


            Cliente cliente = new Cliente();
            cliente.setNombre(nombreCliente);
            cliente.setTelefono(telefono1);
            cliente.setDireccion(direccionEntrega);


            List<Detallerenta> detalles = new ArrayList<>();
            for (CarritoItem item : carrito.getItems()) {
                if (item.getArticulo() == null) continue;

                Detallerenta det = new Detallerenta();
                det.setArticulo(item.getArticulo());
                det.setCantidad(item.getCantidad());
                det.setPrecioUnitario(item.getArticulo().getPrecio());
                // det.setPrecioTotal(item.getPrecioTotal());
                detalles.add(det);
            }

            LocalDate fecha = (fechaEntrega != null)
                    ? fechaEntrega.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    : LocalDate.now();
            LocalTime hora = LocalTime.now();

            String estado = "SOLICITADA";


            facadeRenta.registrarRenta(cliente, detalles, fecha, hora, estado);


            carrito.vaciar();
            aceptaTerminos = false;
            nombreCliente = null;
            telefono1 = null;
            direccionEntrega = null;
            fechaEntrega = null;

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Éxito",
                    "La renta se registró correctamente."
            ));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Ocurrió un error al registrar la renta: " + e.getMessage()
            ));
            ctx.validationFailed();
        }
    }



    public Carrito getCarrito() {
        return carrito;
    }


    public BigDecimal getTotal() {
        return carrito.getTotal();
    }

    public boolean isAceptaTerminos() {
        return aceptaTerminos;
    }

    public void setAceptaTerminos(boolean aceptaTerminos) {
        this.aceptaTerminos = aceptaTerminos;
    }

    public List<Articulo> getCatalogoSimulado() {
        return catalogoSimulado;
    }

    public Date getFechaActual() {
        return new Date();
    }


    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTelefono1() { return telefono1; }
    public void setTelefono1(String telefono1) { this.telefono1 = telefono1; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public Date getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(Date fechaEntrega) { this.fechaEntrega = fechaEntrega; }
}