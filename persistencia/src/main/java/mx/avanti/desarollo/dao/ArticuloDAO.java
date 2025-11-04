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

    public List<Articulo> obtenerTodos(){
        return entityManager
                .createQuery("SELECT a FROM Articulo a", Articulo.class)
                .getResultList();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

}
