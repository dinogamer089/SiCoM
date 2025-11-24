package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Cliente;

public class ClienteDAO extends AbstractDAO<Cliente> {

    private final EntityManager entityManager;

    public ClienteDAO(EntityManager em) {
        super(Cliente.class);
        this.entityManager = em;
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
