package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.MovimientoAlmacen;
import mx.desarollo.entity.TipoMovimiento;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO para gestionar los movimientos de almacen.
 */
public class MovimientoAlmacenDAO extends AbstractDAO<MovimientoAlmacen> {

    private EntityManager em;

    public MovimientoAlmacenDAO(EntityManager entityManager) {
        super(MovimientoAlmacen.class);
        this.em = entityManager;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }


    public List<MovimientoAlmacen> obtenerPorArticuloYFecha(Integer idArticulo, LocalDate fecha) {
        return em.createQuery(
            "SELECT m FROM MovimientoAlmacen m " +
            "WHERE m.articulo.id = :idArticulo AND m.fecha = :fecha " +
            "ORDER BY m.fechaHoraRegistro ASC",
            MovimientoAlmacen.class)
            .setParameter("idArticulo", idArticulo)
            .setParameter("fecha", fecha)
            .getResultList();
    }


    public List<MovimientoAlmacen> obtenerPorArticuloYRango(Integer idArticulo, LocalDate fechaInicio, LocalDate fechaFin) {
        return em.createQuery(
            "SELECT m FROM MovimientoAlmacen m " +
            "WHERE m.articulo.id = :idArticulo " +
            "AND m.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY m.fecha ASC, m.fechaHoraRegistro ASC",
            MovimientoAlmacen.class)
            .setParameter("idArticulo", idArticulo)
            .setParameter("fechaInicio", fechaInicio)
            .setParameter("fechaFin", fechaFin)
            .getResultList();
    }


    public int calcularTotalEntradas(Integer idArticulo, LocalDate fecha) {
        Long result = em.createQuery(
            "SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoAlmacen m " +
            "WHERE m.articulo.id = :idArticulo " +
            "AND m.fecha = :fecha " +
            "AND m.tipoMovimiento = :tipo",
            Long.class)
            .setParameter("idArticulo", idArticulo)
            .setParameter("fecha", fecha)
            .setParameter("tipo", TipoMovimiento.ENTRADA)
            .getSingleResult();
        return result.intValue();
    }


    public int calcularTotalSalidas(Integer idArticulo, LocalDate fecha) {
        Long result = em.createQuery(
            "SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoAlmacen m " +
            "WHERE m.articulo.id = :idArticulo " +
            "AND m.fecha = :fecha " +
            "AND m.tipoMovimiento = :tipo",
            Long.class)
            .setParameter("idArticulo", idArticulo)
            .setParameter("fecha", fecha)
            .setParameter("tipo", TipoMovimiento.SALIDA)
            .getSingleResult();
        return result.intValue();
    }


    public List<MovimientoAlmacen> obtenerPorRenta(Integer idRenta) {
        return em.createQuery(
            "SELECT m FROM MovimientoAlmacen m " +
            "WHERE m.renta.id = :idRenta " +
            "ORDER BY m.fechaHoraRegistro ASC",
            MovimientoAlmacen.class)
            .setParameter("idRenta", idRenta)
            .getResultList();
    }
}
