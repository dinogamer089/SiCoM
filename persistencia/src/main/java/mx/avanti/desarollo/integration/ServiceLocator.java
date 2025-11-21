package mx.avanti.desarollo.integration;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.dao.*;
import mx.avanti.desarollo.persistence.HibernateUtil;

public class ServiceLocator {

    private static EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }

    public static AdministradorDAO getInstanceAdministradorDAO() {
        return new AdministradorDAO(getEntityManager());
    }

    public static EmpleadoDAO getInstanceEmpleadoDAO() {
        return new EmpleadoDAO(getEntityManager());
    }

    public static ArticuloDAO getInstanceArticuloDAO() {
        return new ArticuloDAO(getEntityManager());
    }


    public static ClienteDAO getInstanceClienteDAO() {
        return new ClienteDAO(getEntityManager());
    }

    public static RentaDAO getInstanceRentaDAO() {
        return new RentaDAO(getEntityManager());
    }

    public static DetallerentaDAO getInstanceDetallerentaDAO() {
        return new DetallerentaDAO(getEntityManager());
    }

    public static StockReservadoDAO getInstanceStockReservadoDAO() {
        return new StockReservadoDAO(getEntityManager());
    }
}
