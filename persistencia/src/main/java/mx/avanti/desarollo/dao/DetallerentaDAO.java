package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Detallerenta;

public class DetallerentaDAO extends AbstractDAO<Detallerenta> {

    private final EntityManager entityManager;

    public DetallerentaDAO(EntityManager em) {
        super(Detallerenta.class);
        this.entityManager = em;
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
