package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Administrador;

public class AdministradorDAO extends AbstractDAO<Administrador> {
    private final EntityManager entityManager;

    public AdministradorDAO(EntityManager em) {
        super(Administrador.class);
        this.entityManager = em;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Administrador findByCorreo(String correo) {
        try {
            return entityManager.createQuery(
                            "SELECT a FROM Administrador a WHERE a.correo = :correo", Administrador.class)
                    .setParameter("correo", correo)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}