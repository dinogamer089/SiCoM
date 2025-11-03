package mx.avanti.desarollo.integration;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.dao.*;
import mx.avanti.desarollo.persistence.HibernateUtil;

public class ServiceLocator {
    private static AdministradorDAO administradorDAO;
    private static EmpleadoDAO empleadoDAO;
    private static ArticuloDAO articuloDAO;
    private static ClienteDAO clienteDAO;
    private static RentaDAO rentaDAO;
    private static DetalleDAO detalleDAO;

    private static EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }

    public static AdministradorDAO getInstanceAdministradorDAO() {
        if (administradorDAO == null) {
            administradorDAO = new AdministradorDAO(getEntityManager());
        }
        return administradorDAO;
    }

    public static EmpleadoDAO getInstanceEmpleadoDAO() {
        if (empleadoDAO == null) {
            empleadoDAO = new EmpleadoDAO(getEntityManager());
        }
        return empleadoDAO;
    }

    public static ArticuloDAO getInstanceArticuloDAO() {
        if (articuloDAO == null) {
            articuloDAO = new ArticuloDAO(getEntityManager());
        }
        return articuloDAO;
    }

    public static ClienteDAO getInstanceClienteDAO() {
        if (clienteDAO == null) {
            clienteDAO = new ClienteDAO(getEntityManager());
        }
        return clienteDAO;
    }

    public static RentaDAO getInstanceRentaDAO() {
        if (rentaDAO == null) {
            rentaDAO = new RentaDAO(getEntityManager());
        }
        return rentaDAO;
    }

    public static DetalleDAO getInstanceDetalleDAO() {
        if (detalleDAO == null) {
            detalleDAO = new DetalleDAO(getEntityManager());
        }
        return detalleDAO;
    }
}