package mx.desarollo.integration;

import mx.desarollo.facade.FacadeEmpleado;
import mx.desarollo.facade.FacadeLogin;
import mx.desarollo.facade.FacadeArticulo;
import mx.desarollo.facade.FacadeCombinacionMesa;
import mx.desarollo.facade.FacadeRenta;
import mx.desarollo.facade.FacadeTarjetaAlmacen;

public class ServiceFacadeLocator {
    private static FacadeLogin facadeLogin;
    private static FacadeArticulo facadeArticulo;
    private static FacadeRenta facadeRenta;
    private static FacadeEmpleado facadeEmpleado;
    private static FacadeCombinacionMesa facadeCombinacionMesa;
    private static FacadeTarjetaAlmacen facadeTarjetaAlmacen;

    public static FacadeLogin getInstanceFacadeLogin() {
        if (facadeLogin == null) {
            facadeLogin = new FacadeLogin();
        }
        return facadeLogin;
    }

    public static FacadeArticulo getInstanceFacadeArticulo() {
        if (facadeArticulo == null) {
            facadeArticulo = new FacadeArticulo();
        }
        return facadeArticulo;
    }

    public static FacadeRenta getInstanceFacadeRenta() {
        if (facadeRenta == null) {
            facadeRenta = new FacadeRenta();
        }
        return facadeRenta;
    }

    public static FacadeEmpleado getInstanceFacadeEmpleado() {
        if (facadeEmpleado == null) {
            facadeEmpleado = new FacadeEmpleado();
        }
        return facadeEmpleado;
    }

    public static FacadeCombinacionMesa getInstanceFacadeCombinacionMesa() {
        if (facadeCombinacionMesa == null) {
            facadeCombinacionMesa = new FacadeCombinacionMesa();
        }
        return facadeCombinacionMesa;
    }

    public static FacadeTarjetaAlmacen getInstanceFacadeTarjetaAlmacen() {
        if (facadeTarjetaAlmacen == null) {
            facadeTarjetaAlmacen = new FacadeTarjetaAlmacen();
        }
        return facadeTarjetaAlmacen;
    }
}