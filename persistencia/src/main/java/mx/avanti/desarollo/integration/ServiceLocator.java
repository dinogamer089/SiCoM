package mx.avanti.desarollo.integration;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.dao.*;
import mx.avanti.desarollo.persistence.HibernateUtil;

public class ServiceLocator {

    private static EntityManager getEntityManager(){
        return HibernateUtil.getEntityManager();
    }


    public static UsuarioDAO getInstanceUsuarioDAO(){
        return new UsuarioDAO(getEntityManager());
    }

    public static ArticuloDAO getInstanceArticuloDAO(){
        return new ArticuloDAO(getEntityManager());
    }
}