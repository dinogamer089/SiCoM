package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Detallerenta;

import java.util.List;

public class DetalleDAO extends AbstractDAO<Detallerenta> {
    private final EntityManager entityManager;

    public DetalleDAO(EntityManager em) {
        super(Detallerenta.class);
        this.entityManager = em;
    }

    public List<Detallerenta> obtenerTodos(){
        return entityManager
                .createQuery("SELECT d FROM Detallerenta d", Detallerenta.class)
                .getResultList();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}