package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.MovimientoAlmacen;
import mx.desarollo.entity.StockDiario;

import java.time.LocalDate;
import java.util.List;

/**
 * Delegate para operaciones de tarjeta de almacen.
 * Coordina llamadas entre StockDiarioDAO y MovimientoAlmacenDAO.
 */
public class DelegateTarjetaAlmacen {

    /**
     * Obtiene todos los movimientos de un articulo en una fecha especifica.
     * @param idArticulo ID del articulo
     * @param fecha Fecha a consultar
     * @return Lista de movimientos
     */
    public List<MovimientoAlmacen> obtenerMovimientosPorFecha(Integer idArticulo, LocalDate fecha) {
        return ServiceLocator.getInstanceMovimientoAlmacenDAO()
            .obtenerPorArticuloYFecha(idArticulo, fecha);
    }


    public List<MovimientoAlmacen> obtenerMovimientosPorRango(Integer idArticulo, LocalDate fechaInicio, LocalDate fechaFin) {
        return ServiceLocator.getInstanceMovimientoAlmacenDAO()
            .obtenerPorArticuloYRango(idArticulo, fechaInicio, fechaFin);
    }


    public int obtenerInventarioInicial(Integer idArticulo, LocalDate fecha) {
        return ServiceLocator.getInstanceStockDiarioDAO()
            .obtenerInventarioInicial(idArticulo, fecha);
    }


    public int calcularEntradas(Integer idArticulo, LocalDate fecha) {
        return ServiceLocator.getInstanceMovimientoAlmacenDAO()
            .calcularTotalEntradas(idArticulo, fecha);
    }


    public int calcularSalidas(Integer idArticulo, LocalDate fecha) {
        return ServiceLocator.getInstanceMovimientoAlmacenDAO()
            .calcularTotalSalidas(idArticulo, fecha);
    }


    public void registrarMovimiento(MovimientoAlmacen movimiento) {
        ServiceLocator.getInstanceMovimientoAlmacenDAO().save(movimiento);
    }


    public StockDiario obtenerOCrearStockDiario(Integer idArticulo, LocalDate fecha) {
        var stockDAO = ServiceLocator.getInstanceStockDiarioDAO();
        return stockDAO.obtenerPorArticuloYFecha(idArticulo, fecha)
            .orElseGet(() -> {
                StockDiario nuevo = new StockDiario();
                nuevo.setArticulo(ServiceLocator.getInstanceArticuloDAO()
                    .find(idArticulo).orElse(null));
                nuevo.setFecha(fecha);
                nuevo.setInventarioInicial(0);
                nuevo.setExistenciaFinal(0);
                stockDAO.save(nuevo);
                return nuevo;
            });
    }
}
