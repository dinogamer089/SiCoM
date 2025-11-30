package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

public class DelegateRenta {
    public List<Renta> findAllCotizaciones(){
        return ServiceLocator.getInstanceRentaDAO().obtenerTodosCotizaciones();
    }

    public Renta findRentaId(Integer idRenta){
        return ServiceLocator.getInstanceRentaDAO().obtenerRentaID(idRenta);
    }

    public void cambiarEstado(Integer idRenta, String nuevoEstado){
        // Cambiar el estado de la renta
        ServiceLocator.getInstanceRentaDAO().cambiarEstadoRenta(idRenta, nuevoEstado);

        // Registrar movimientos automáticos según el estado
        registrarMovimientosAutomaticos(idRenta, nuevoEstado);
    }

    /**
     * Registra movimientos de almacén automáticamente según el estado de la renta.
     */
    private void registrarMovimientosAutomaticos(Integer idRenta, String nuevoEstado) {

        if (!"Confirmado".equalsIgnoreCase(nuevoEstado) && !"Finalizada".equalsIgnoreCase(nuevoEstado)) {
            return;
        }

        try {

            Renta renta = ServiceLocator.getInstanceRentaDAO().obtenerRentaID(idRenta);
            if (renta == null || renta.getDetallesRenta() == null || renta.getDetallesRenta().isEmpty()) {
                System.err.println("No se encontró la renta o no tiene detalles: " + idRenta);
                return;
            }

            LocalDate fechaMovimiento = renta.getFecha() != null ? renta.getFecha() : LocalDate.now();
            TipoMovimiento tipoMovimiento;
            String conceptoBase;

            if ("Confirmado".equalsIgnoreCase(nuevoEstado)) {
                tipoMovimiento = TipoMovimiento.SALIDA;
                conceptoBase = "Salida por Renta #" + idRenta + " (Confirmado)";
            } else { // Finalizada
                tipoMovimiento = TipoMovimiento.ENTRADA;
                conceptoBase = "Devolución Renta #" + idRenta + " (Finalizada)";
            }

            // Registrar un movimiento por cada artículo en la renta
            for (Detallerenta detalle : renta.getDetallesRenta()) {
                MovimientoAlmacen movimiento = new MovimientoAlmacen();
                movimiento.setArticulo(detalle.getIdarticulo());
                movimiento.setRenta(renta);
                movimiento.setFecha(fechaMovimiento);
                movimiento.setFechaHoraRegistro(LocalDateTime.now());
                movimiento.setTipoMovimiento(tipoMovimiento);
                movimiento.setCantidad(detalle.getCantidad());
                movimiento.setPrecioUnitario(detalle.getPrecioUnitario());
                movimiento.setConcepto(conceptoBase + " - " + detalle.getIdarticulo().getNombre());

                // Guardar el movimiento
                ServiceLocator.getInstanceMovimientoAlmacenDAO().save(movimiento);

                System.out.println("Movimiento registrado: " + tipoMovimiento + " de " +
                        detalle.getCantidad() + " unidades de " +
                        detalle.getIdarticulo().getNombre() + " para Renta #" + idRenta);
            }

            // Actualizar o crear el stock diario para cada artículo
            actualizarStockDiario(renta.getDetallesRenta(), fechaMovimiento);

        } catch (Exception e) {
            System.err.println("Error al registrar movimientos automáticos para renta " + idRenta + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza o crea el registro de stock diario para los artículos de la renta.
     */
    private void actualizarStockDiario(List<Detallerenta> detalles, LocalDate fecha) {
        for (Detallerenta detalle : detalles) {
            try {
                Integer idArticulo = detalle.getIdarticulo().getId();
                var stockDAO = ServiceLocator.getInstanceStockDiarioDAO();

                // Verificar si ya existe el registro de stock diario
                var stockOpt = stockDAO.obtenerPorArticuloYFecha(idArticulo, fecha);

                if (stockOpt.isEmpty()) {
                    // No existe, crear uno nuevo basado en el inventario actual del artículo
                    StockDiario nuevoStock = new StockDiario();
                    nuevoStock.setArticulo(detalle.getIdarticulo());
                    nuevoStock.setFecha(fecha);

                    Integer unidadesActuales = detalle.getIdarticulo().getUnidades();
                    nuevoStock.setInventarioInicial(unidadesActuales != null ? unidadesActuales : 0);
                    nuevoStock.setExistenciaFinal(unidadesActuales != null ? unidadesActuales : 0);

                    stockDAO.save(nuevoStock);
                    System.out.println("Stock diario creado para artículo " + idArticulo + " en fecha " + fecha);
                }
            } catch (Exception e) {
                System.err.println("Error al actualizar stock diario: " + e.getMessage());
            }
        }
    }

    public List<Renta> findAllRentas(){
        return ServiceLocator.getInstanceRentaDAO().obtenerTodosRentas();
    }

    public void actualizarRenta(Renta renta){
        ServiceLocator.getInstanceRentaDAO().update(renta);
    }


    public List<Renta> obtenerRentasDisponiblesYAsignadas(Integer idEmpleado){
        return ServiceLocator.getInstanceRentaDAO().obtenerRentasDisponiblesYAsignadas(idEmpleado);
    }

    // Delegación para registrar una renta/cotización desde el carrito
    public void registrarRenta(Cliente cliente,
                               List<Detallerenta> detalles,
                               LocalDate fecha,
                               LocalTime hora,
                               String estado) {
        ServiceLocator.getInstanceRentaDAO().registrarRenta(cliente, detalles, fecha, hora, estado);
    }

    public List<Comentario> obtenerComentarios(Integer idRenta) {
        return ServiceLocator.getInstanceRentaDAO().obtenerComentarios(idRenta);
    }

    public void guardarComentario(Comentario comentario) {
        ServiceLocator.getInstanceRentaDAO().guardarComentario(comentario);
    }
}