package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.sql.Date;
import java.time.LocalDate;

/**
 * DAO utilitario para consultar la tabla auxiliar stock_reservado_diario
 */
public class StockReservadoDAO {

    private final EntityManager entityManager;

    public StockReservadoDAO(EntityManager em) {
        this.entityManager = em;
    }

    /**
     * Obtiene la cantidad reservada para un artículo en una fecha específica.
     * Si no hay registro, devuelve 0.
     *
     * @param idArticulo ID del artículo
     * @param fecha      Fecha a consultar
     * @return Cantidad reservada o 0 si no hay registro
     */
    public int obtenerReservado(int idArticulo, LocalDate fecha) {
        if (fecha == null) {
            return 0;
        }

        try {
            Object single = entityManager.createNativeQuery(
                            "SELECT cantidad_reservada " +
                                    "FROM stock_reservado_diario " +
                                    "WHERE idarticulo = ?1 AND fecha = ?2")
                    .setParameter(1, idArticulo)
                    .setParameter(2, Date.valueOf(fecha))
                    .getSingleResult();

            if (single == null) {
                return 0;
            }
            if (single instanceof Number) {
                return ((Number) single).intValue();
            }
            return Integer.parseInt(single.toString());

        } catch (NoResultException ex) {
            return 0;
        }
    }
}