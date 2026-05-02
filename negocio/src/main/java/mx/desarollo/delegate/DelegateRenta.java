package mx.desarollo.delegate;

import mx.avanti.desarollo.dao.StockReservadoDAO;
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

        if (!"Confirmado".equalsIgnoreCase(nuevoEstado)
                && !"Finalizada".equalsIgnoreCase(nuevoEstado)
                && !"Cancelada".equalsIgnoreCase(nuevoEstado)) {
            return;
        }

        try {

            Renta renta = ServiceLocator.getInstanceRentaDAO().obtenerRentaID(idRenta);
            if (renta == null || renta.getDetallesRenta() == null || renta.getDetallesRenta().isEmpty()) {
                System.err.println("No se encontró la renta o no tiene detalles: " + idRenta);
                return;
            }

            // Reglas de negocio para el rango de fechas
            LocalDate fechaMovimiento;
            TipoMovimiento tipoMovimiento;
            String conceptoBase;

            if ("Confirmado".equalsIgnoreCase(nuevoEstado)) {
                tipoMovimiento = TipoMovimiento.SALIDA;
                conceptoBase = "Salida por Renta #" + idRenta + " (Confirmado)";
                fechaMovimiento = renta.getFechaInicio() != null ? renta.getFechaInicio()
                        : (renta.getFecha() != null ? renta.getFecha() : LocalDate.now());
            } else if ("Finalizada".equalsIgnoreCase(nuevoEstado)) {
                tipoMovimiento = TipoMovimiento.ENTRADA;
                conceptoBase = "Devolución Renta #" + idRenta + " (Finalizada)";
                fechaMovimiento = renta.getFecha() != null ? renta.getFecha() : LocalDate.now();
            } else { // Cancelada
                tipoMovimiento = TipoMovimiento.ENTRADA;
                conceptoBase = "Devolución Renta #" + idRenta + " (Cancelada)";
                fechaMovimiento = renta.getFecha() != null ? renta.getFecha() : LocalDate.now();
            }

            // Registrar un movimiento por cada artículo en la renta
            for (Detallerenta detalle : renta.getDetallesRenta()) {

                // GESTIÓN DE STOCK RESERVADO
                // NOTA: El stored procedure 'cambiar_estado_renta' ya maneja la RESERVA de stock
                // cuando el estado cambia de SOLICITADA → Aprobada, por lo tanto NO reservamos
                // aquí en "Confirmado" para evitar duplicación.
                // Solo manejamos la LIBERACIÓN de stock al finalizar o cancelar.
                if ("Finalizada".equalsIgnoreCase(nuevoEstado) || "Cancelada".equalsIgnoreCase(nuevoEstado)) {
                    // Liberar stock al finalizar o cancelar
                    liberarStock(detalle.getIdarticulo().getId(), renta.getFecha(), detalle.getCantidad());
                }

                // REGISTRO DE MOVIMIENTO DE ALMACÉN (solo para Confirmado y Finalizada, no Cancelada sin confirmar)
                if (!"Cancelada".equalsIgnoreCase(nuevoEstado) || tipoMovimiento == TipoMovimiento.ENTRADA) {
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
            }

            // Actualizar o crear el stock diario para cada artículo (solo para Confirmado y Finalizada)
            if (!"Cancelada".equalsIgnoreCase(nuevoEstado)) {
                actualizarStockDiario(renta.getDetallesRenta(), fechaMovimiento);
            }

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
                               LocalDate fechaInicio,
                               LocalDate fecha,
                               LocalTime hora,
                               String estado) {
        ServiceLocator.getInstanceRentaDAO().registrarRenta(cliente, detalles, fechaInicio, fecha, hora, estado);
    }

    public List<Comentario> obtenerComentarios(Integer idRenta) {
        return ServiceLocator.getInstanceComentarioDAO().obtenerComentarios(idRenta);
    }

    public void guardarComentario(Comentario comentario) {
        ServiceLocator.getInstanceComentarioDAO().guardarComentario(comentario);
    }

    public void actualizarRentaConStock(Renta renta, LocalDate fechaInicioAnterior, LocalDate fechaAnterior) throws Exception {
        ServiceLocator.getInstanceRentaDAO().actualizarRentaConStock(renta, fechaInicioAnterior, fechaAnterior);
    }

    /**
     * Libera stock reservado de un artículo en una fecha específica.
     * Elimina o reduce registro en StockReservadoDiario.
     * <p>
     * NOTA: Este método se usa porque el stored procedure 'cambiar_estado_renta'
     * solo libera stock cuando el cambio es directamente Aprobada→Finalizada,
     * pero en el flujo real de estados, la renta pasa por varios estados intermedios
     * (Confirmado → ... → En recoleccion → Finalizada), por lo que el SP no se activa.
     *
     * @param idArticulo ID del artículo
     * @param fecha Fecha de la reserva
     * @param cantidad Cantidad a liberar
     */
    private void liberarStock(Integer idArticulo, LocalDate fecha, Integer cantidad) {
        if (idArticulo == null || fecha == null || cantidad == null || cantidad <= 0) {
            return;
        }

        try {
            StockReservadoDAO stockReservadoDAO = ServiceLocator.getInstanceStockReservadoDAO();
            stockReservadoDAO.ajustarStockReservado(idArticulo, fecha, -cantidad); // Negativo para liberar
            System.out.println("Stock liberado: " + cantidad + " unidades del artículo #" +
                    idArticulo + " para fecha " + fecha);
        } catch (Exception e) {
            System.err.println("Error al liberar stock: " + e.getMessage());
            e.printStackTrace();
        }
    }
}