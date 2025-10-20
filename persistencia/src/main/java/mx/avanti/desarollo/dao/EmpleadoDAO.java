package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Empleado;

public class EmpleadoDAO extends AbstractDAO<Empleado> {
    private final EntityManager entityManager;

    public EmpleadoDAO(EntityManager em) {
        super(Empleado.class);
        this.entityManager = em;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}