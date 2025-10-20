package mx.avanti.desarollo.integration;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.dao.*;
import mx.avanti.desarollo.persistence.HibernateUtil;

public class ServiceLocator {
    private static AdministradorDAO administradorDAO;
    private static EmpleadoDAO empleadoDAO;

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
        return new ArticuloDAO(getEntityManager());
    }

}