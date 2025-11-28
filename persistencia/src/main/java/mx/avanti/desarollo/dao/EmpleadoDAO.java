package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Empleado;

import java.util.List;

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

    public Empleado findByCorreo(String correo) {
        try {
            return entityManager.createQuery(
                            "SELECT e FROM Empleado e WHERE e.correo = :correo", Empleado.class)
                    .setParameter("correo", correo)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Empleado findById(Integer id) {
        try {

            return entityManager.createQuery(
                            "SELECT e FROM Empleado e WHERE e.id = :id", Empleado.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}