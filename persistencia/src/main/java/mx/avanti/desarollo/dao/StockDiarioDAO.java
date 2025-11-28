package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.StockDiario;

import java.time.LocalDate;
import java.util.Optional;

/**
 * DAO para gestionar el stock diario de articulos.
 */
public class StockDiarioDAO extends AbstractDAO<StockDiario> {

    public StockDiarioDAO(EntityManager entityManager) {
        super(StockDiario.class);
    }

    private EntityManager em;

    public StockDiarioDAO(EntityManager entityManager, boolean useField) {
        super(StockDiario.class);
        this.em = entityManager;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }


    public Optional<StockDiario> obtenerPorArticuloYFecha(Integer idArticulo, LocalDate fecha) {
        try {
            StockDiario result = em.createQuery(
                "SELECT s FROM StockDiario s WHERE s.articulo.id = :idArticulo AND s.fecha = :fecha",
                StockDiario.class)
                .setParameter("idArticulo", idArticulo)
                .setParameter("fecha", fecha)
                .getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    public int obtenerInventarioInicial(Integer idArticulo, LocalDate fecha) {
        return obtenerPorArticuloYFecha(idArticulo, fecha)
            .map(StockDiario::getInventarioInicial)
            .orElse(0);
    }
}
