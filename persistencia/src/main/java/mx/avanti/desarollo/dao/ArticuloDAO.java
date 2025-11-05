package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Articulo;

import java.util.List;

public class ArticuloDAO extends AbstractDAO<Articulo> {
    private final EntityManager entityManager;

    public ArticuloDAO(EntityManager em) {
        super(Articulo.class);
        this.entityManager = em;
    }

    public List<Articulo> obtenerTodos() {
        String jpql = "SELECT DISTINCT a FROM Articulo a LEFT JOIN FETCH a.imagen";
        return execute(em -> em.createQuery(jpql, Articulo.class).getResultList());
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}