/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.avanti.desarollo.integration;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.dao.*;
import mx.avanti.desarollo.persistence.HibernateUtil;


/**
 *
 * @author total
 */
public class ServiceLocator {

    private static ArticuloDAO articuloDAO;


    private static EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }


    public static ArticuloDAO getInstanceArticuloDAO() {
        if (articuloDAO == null) {
            articuloDAO = new ArticuloDAO(getEntityManager());
            return articuloDAO;
        } else {
            return articuloDAO;
        }
    }

}
