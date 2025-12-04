package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Comentario;

import java.util.Collections;
import java.util.List;

public class ComentarioDAO extends AbstractDAO<Comentario> {
    private final EntityManager entityManager;

    public ComentarioDAO(EntityManager em) {
        super(Comentario.class);
        this.entityManager = em;
    }

    public List<Comentario> obtenerComentarios(Integer idRenta) {
        if (idRenta == null) {
            return Collections.emptyList();
        }

        try {
            return entityManager.createQuery(
                            "SELECT c FROM Comentario c WHERE c.idRenta.id = :idRenta", Comentario.class)
                    .setParameter("idRenta", idRenta)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void guardarComentario(Comentario comentario) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(comentario);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
