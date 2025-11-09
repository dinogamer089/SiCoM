package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Renta;

import java.util.List;

public class RentaDAO extends AbstractDAO<Renta> {
    private final EntityManager entityManager;

    public RentaDAO(EntityManager em) {
        super(Renta.class);
        this.entityManager = em;
    }

    public List<Renta> obtenerTodosCotizaciones(){
        return entityManager
                .createQuery("SELECT r FROM Renta r " +
                        "LEFT JOIN FETCH r.detallesRenta dr " +
                        "LEFT JOIN FETCH dr.idArticulo " +
                        "WHERE r.estado = 'Pendiente por aprobar' " +
                        "ORDER BY r.id", Renta.class)
                .getResultList();
    }

    public List<Renta> obtenerTodosRentas(){
        return entityManager
                .createQuery("SELECT r FROM Renta r " +
                        "LEFT JOIN FETCH r.detallesRenta dr " +
                        "LEFT JOIN FETCH dr.idArticulo " +
                        "WHERE r.estado != 'Pendiente por aprobar' " +
                        "ORDER BY r.id", Renta.class)
                .getResultList();
    }

    public Renta obtenerRentaID(Integer idRenta) {
        try {
            return entityManager.createQuery("SELECT r FROM Renta r " +
                            "LEFT JOIN FETCH r.idCliente " +
                            "LEFT JOIN FETCH r.detallesRenta dr " +
                            "LEFT JOIN FETCH dr.idArticulo " +
                            "WHERE r.id = :id", Renta.class)
                    .setParameter("id", idRenta)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}