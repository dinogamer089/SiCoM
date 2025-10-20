package mx.avanti.desarollo.integration;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.dao.*;
import mx.avanti.desarollo.persistence.HibernateUtil;

/**
 *
 * @author total
 */
public class ServiceLocator {
    private static UsuarioDAO usuarioDAO;
    private static EmpleadoDAO empleadoDAO;

    private static EntityManager getEntityManager(){
        return HibernateUtil.getEntityManager();
    }

    /**
     * se crea la instancia de usuarioDAO si esta no existe
     */
    public static UsuarioDAO getInstanceUsuarioDAO(){
        if(usuarioDAO == null){
            usuarioDAO = new UsuarioDAO(getEntityManager());
            return usuarioDAO;
        } else{
            return usuarioDAO;
        }
    }

    /**
     * se crea la instancia de empleadoDAO si esta no existe
     */
    public static EmpleadoDAO getInstanceEmpleadoDAO(){
        if(empleadoDAO == null){
            empleadoDAO = new EmpleadoDAO(getEntityManager());
            return empleadoDAO;
        } else{
            return empleadoDAO;
        }
    }
}