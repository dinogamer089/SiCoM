package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;

import java.util.List;

public class ArticuloDAO extends AbstractDAO<Articulo> {
    private final EntityManager entityManager;

    public ArticuloDAO(EntityManager em) {
        super(Articulo.class);
        this.entityManager = em;
    }

    public List<Articulo> obtenerTodos() {
        return execute(em -> {
            String jpql = "SELECT a FROM Articulo a";
            return em.createQuery(jpql, Articulo.class).getResultList();
        });
    }

    public void saveWithImage(Articulo articulo, Imagen imagen) {
        execute(em -> {
            em.persist(imagen);
            em.flush();
            articulo.setImagen(imagen);
            em.persist(articulo);
            return null;
        });
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}