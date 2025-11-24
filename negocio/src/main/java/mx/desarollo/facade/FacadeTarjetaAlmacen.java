package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateTarjetaAlmacen;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.MovimientoAlmacen;

import java.time.LocalDate;
import java.util.List;


public class FacadeTarjetaAlmacen {

    private final DelegateTarjetaAlmacen delegate;

    public FacadeTarjetaAlmacen() {
        this.delegate = new DelegateTarjetaAlmacen();
    }


    public List<MovimientoAlmacen> consultarMovimientosPorFecha(Integer idArticulo, LocalDate fecha) {
        return delegate.obtenerMovimientosPorFecha(idArticulo, fecha);
    }


    public List<MovimientoAlmacen> consultarMovimientosPorRango(Integer idArticulo, LocalDate fechaInicio, LocalDate fechaFin) {
        return delegate.obtenerMovimientosPorRango(idArticulo, fechaInicio, fechaFin);
    }


    public int obtenerInventarioInicial(Integer idArticulo, LocalDate fecha) {
        return delegate.obtenerInventarioInicial(idArticulo, fecha);
    }


    public int[] calcularTotalesDiarios(Integer idArticulo, LocalDate fecha) {
        int entradas = delegate.calcularEntradas(idArticulo, fecha);
        int salidas = delegate.calcularSalidas(idArticulo, fecha);
        return new int[]{entradas, salidas};
    }


    public void registrarMovimiento(MovimientoAlmacen movimiento) {
        delegate.registrarMovimiento(movimiento);
    }
}
