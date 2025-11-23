package ui;

import helper.Carrito;
import helper.CarritoItem;
import helper.dto.ArticuloCardDTO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;

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
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    // Fecha seleccionada previamente en el catálogo
    private Date fechaCatalogoSeleccionada;

    // Notificaciones/flags por ajustes de stock al cambiar la fecha
    private List<String> notificacionesStock;


    private FacadeRenta facadeRenta;

    @PostConstruct
    public void init() {
        carrito = new Carrito();
        aceptaTerminos = false;
        notificacionesStock = new ArrayList<>();

        facadeRenta = ServiceFacadeLocator.getInstanceFacadeRenta();

        // Sincroniza la fecha del carrito con la seleccionada en el catálogo si existe
        sincronizarFechaConCatalogo();
    }



    private void agregarArticuloPorId(int idArticulo) {
        if (catalogoSimulado == null) return;

        Integer idObj = idArticulo;
        for (Articulo art : catalogoSimulado) {
            if (art != null
                    && art.getId() != null
                    && idObj.equals(art.getId())) {

                boolean added = carrito.agregarArticulo(art);
                if (added) {
                    System.out.println("Artículo id=" + idArticulo
                            + " agregado al carrito (stock=" + art.getUnidades() + ").");
                } else {
                    System.out.println("No se pudo agregar al carrito el artículo id=" + idArticulo
                            + " (posible falta de stock).");
                }
                break;
            }
        }
    }


    public void agregarArticulo(ArticuloCardDTO articulo) {
        // Asegura que el carrito esté usando la fecha seleccionada en el catálogo
        sincronizarFechaConCatalogo();
        if (articulo == null) return;
        // Adaptar DTO -> Entidad para respetar la lógica de stock
        mx.desarollo.entity.Articulo entidad = null;
        try {
            var opt = mx.desarollo.integration.ServiceFacadeLocator
                    .getInstanceFacadeArticulo()
                    .obtenerArticuloConImagenPorId(articulo.getId());
            if (opt != null && opt.isPresent()) {
                entidad = opt.get();
            }
        } catch (Exception ignored) {}
        if (entidad == null) {
            // Fallback mínimo si no se encuentra: no agregar para evitar inconsistencias de stock
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Artículo no disponible",
                    "No fue posible obtener el artículo seleccionado."
            ));
            return;
        }
        boolean added = carrito.agregarArticulo(entidad);
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
        sincronizarFechaConCatalogo();
        carrito.incrementarCantidad(item);
    }

    public void decrementarCantidad(CarritoItem item) {
        sincronizarFechaConCatalogo();
        carrito.decrementarCantidad(item);
    }

    public void vaciarCarrito() {
        carrito.vaciar();
    }

    // Aplica una cantidad ingresada manualmente respetando el stock disponible
    public void aplicarCantidad(CarritoItem item) {
        if (item == null) return;
        sincronizarFechaConCatalogo();
        int n = Math.max(0, item.getCantidad());
        carrito.cambiarCantidad(item, n);
    }

    // Máximo permitido para el ítem (stock disponible en la fecha)
    public int maxCantidadItem(CarritoItem item) {
        try {
            if (item == null || item.getArticulo() == null) return 0;
            return carrito.checkStock(item.getArticulo(), carrito.getFechaSeleccionada());
        } catch (Exception e) {
            return item != null ? item.getCantidad() : 0;
        }
    }

    // (Revertido) Edición manual deshabilitada; mantener solo botones +/-


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

            LocalDate fecha = (carrito.getFechaSeleccionada() != null)
                    ? carrito.getFechaSeleccionada()
                    : (fechaCatalogoSeleccionada != null
                        ? fechaCatalogoSeleccionada.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        : null);

            if (fecha == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Fecha requerida",
                        "Seleccione una fecha en el catálogo antes de cotizar."
                ));
                ctx.validationFailed();
                return;
            }
            LocalTime hora = LocalTime.now();

            String estado = "SOLICITADA";
            System.out.println("[CarritoBean] Confirmar cotizacion con fecha=" + fecha + ", items=" + carrito.getItems().size());


            facadeRenta.registrarRenta(cliente, detalles, fecha, hora, estado);

            // Abrir WhatsApp Web con mensaje prellenado para la cotización
            try {
                String whatsappMsg = buildWhatsappMessage(cliente, fecha, carrito.getTotal());
                String encoded = URLEncoder.encode(whatsappMsg, StandardCharsets.UTF_8);
                String url = "https://wa.me/526862715636?text=" + encoded;
                PrimeFaces.current().executeScript("window.open('" + url + "', '_blank');");
            } catch (Exception wex) {
                wex.printStackTrace();
            }


            carrito.vaciar();
            aceptaTerminos = false;
            nombreCliente = null;
            telefono1 = null;
            direccionEntrega = null;
            fechaCatalogoSeleccionada = null;

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


    private String buildWhatsappMessage(Cliente cliente, LocalDate fecha, BigDecimal total) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaStr = fecha != null ? fecha.format(fmt) : "";

        StringBuilder sb = new StringBuilder();
        sb.append("Hola, deseo una cotización para Evento Campestre.\n");
        sb.append("Nombre: ").append(safe(cliente != null ? cliente.getNombre() : null)).append("\n");
        sb.append("Teléfono: ").append(safe(cliente != null ? cliente.getTelefono() : null)).append("\n");
        sb.append("Dirección de entrega: ").append(safe(cliente != null ? cliente.getDireccion() : null)).append("\n");
        sb.append("Fecha del evento: ").append(fechaStr).append("\n");
        if (total != null) {
            sb.append("Total estimado: $").append(total.toPlainString()).append("\n");
        }
        return sb.toString();
    }

    private String safe(String v) { return v == null ? "" : v.trim(); }

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

    public Date getFechaActual() { return new Date(); }

    // Intenta leer la fecha seleccionada en el catálogo y aplicarla al carrito
    private void sincronizarFechaConCatalogo() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx == null) return;
            ArticuloCatalogoBeanUI cat = (ArticuloCatalogoBeanUI)
                    ctx.getApplication().evaluateExpressionGet(ctx, "#{articuloCatalogoUI}", ArticuloCatalogoBeanUI.class);
            if (cat != null && cat.getFechaSeleccionada() != null) {
                LocalDate f = cat.getFechaSeleccionada();
                if (carrito.getFechaSeleccionada() == null || !carrito.getFechaSeleccionada().equals(f)) {
                    this.fechaCatalogoSeleccionada = Date.from(f.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    carrito.actualizarFechaSeleccionada(f);
                }
            }
        } catch (Exception ignored) {
        }
    }

    // Setter para que el catálogo actualice la fecha seleccionada
    public void actualizarFechaDesdeCatalogo(Date nuevaFecha) {
        this.fechaCatalogoSeleccionada = nuevaFecha;
        LocalDate f = (nuevaFecha != null)
                ? nuevaFecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;

        // Snapshot de cantidades previas por ID de artículo
        java.util.Map<Integer, Integer> prevCantidades = new java.util.HashMap<>();
        for (CarritoItem it : carrito.getItems()) {
            if (it.getArticulo() != null && it.getArticulo().getId() != null) {
                prevCantidades.put(it.getArticulo().getId(), it.getCantidad());
            }
        }

        java.util.List<String> mensajes = carrito.actualizarFechaSeleccionada(f);
        // Actualiza notificaciones en estado (se mostrarán en un banner dedicado)
        notificacionesStock = new ArrayList<>(mensajes);

        // Marca flags en ítems cuyo stock fue ajustado hacia abajo
        for (CarritoItem it : carrito.getItems()) {
            if (it.getArticulo() == null || it.getArticulo().getId() == null) continue;
            Integer id = it.getArticulo().getId();
            Integer prev = prevCantidades.get(id);
            if (prev != null && it.getCantidad() < prev) {
                it.setAjustadoPorStock(true);
                it.setAvisoAjuste("La cantidad de '" + it.getArticulo().getNombre() + "' se ajustó a " + it.getCantidad() + " por falta de stock para la fecha seleccionada.");
            }
        }

        // Evitar spam de FacesMessages; las notificaciones se muestran agregadas en la vista
    }


    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTelefono1() { return telefono1; }
    public void setTelefono1(String telefono1) { this.telefono1 = telefono1; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public Date getFechaCatalogoSeleccionada() {
        if (this.fechaCatalogoSeleccionada == null) {
            sincronizarFechaConCatalogo();
        }
        return fechaCatalogoSeleccionada;
    }
    public void setFechaCatalogoSeleccionada(Date fechaCatalogoSeleccionada) { actualizarFechaDesdeCatalogo(fechaCatalogoSeleccionada); }

    public List<String> getNotificacionesStock() { return notificacionesStock; }
    public boolean isHuboAjustesStock() { return notificacionesStock != null && !notificacionesStock.isEmpty(); }

    // Permite ocultar/limpiar el banner de notificaciones desde la vista
    public void limpiarNotificacionesStock() {
        if (notificacionesStock != null) {
            notificacionesStock.clear();
        }
    }
}
