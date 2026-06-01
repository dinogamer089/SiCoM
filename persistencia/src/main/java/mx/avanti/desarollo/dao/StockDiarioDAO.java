package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Articulo;
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
        // 1. Intentar obtener el registro del dia exacto
        Optional<StockDiario> stockHoy = obtenerPorArticuloYFecha(idArticulo, fecha);
        if (stockHoy.isPresent()) {
            return stockHoy.get().getInventarioInicial();
        }

        // 2. Si no hay registro hoy, buscar el registro mas reciente ANTES de hoy
        try {
            StockDiario ultimoStock = em.createQuery(
                "SELECT s FROM StockDiario s WHERE s.articulo.id = :idArticulo AND s.fecha < :fecha ORDER BY s.fecha DESC",
                StockDiario.class)
                .setParameter("idArticulo", idArticulo)
                .setParameter("fecha", fecha)
                .setMaxResults(1)
                .getSingleResult();
            
            // El inventario inicial de hoy es la existencia final del ultimo dia registrado
            return ultimoStock.getExistenciaFinal();
        } catch (NoResultException e) {
            // 3. Si no hay registros previos, intentar obtener el stock base del articulo del catalogo
            try {
                Articulo art = em.find(Articulo.class, idArticulo);
                return (art != null && art.getUnidades() != null) ? art.getUnidades() : 0;
            } catch (Exception ex) {
                return 0;
            }
        }
    }
}
