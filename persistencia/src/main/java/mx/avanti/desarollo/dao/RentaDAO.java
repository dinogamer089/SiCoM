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
                        "LEFT JOIN FETCH dr.idarticulo " +
                        "WHERE r.estado = 'SOLICITADA' " +
                        "ORDER BY r.id", Renta.class)
                .getResultList();
    }

    public Renta obtenerRentaID(Integer idRenta) {
        try {
            return entityManager.createQuery("SELECT r FROM Renta r " +
                            "LEFT JOIN FETCH r.idCliente " +
                            "LEFT JOIN FETCH r.detallesRenta dr " +
                            "LEFT JOIN FETCH dr.idarticulo " +
                            "WHERE r.id = :id", Renta.class)
                    .setParameter("id", idRenta)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public void cambiarEstadoRenta(Integer idRenta, String nuevoEstado) {
        try {
            entityManager.getTransaction().begin();

            entityManager.createNativeQuery("CALL cambiar_estado_renta(:idRent, :nuevoEst)")
                    .setParameter("idRent", idRenta)
                    .setParameter("nuevoEst", nuevoEstado)
                    .executeUpdate();

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Renta> obtenerTodosRentas(){
        return entityManager
                .createQuery("SELECT r FROM Renta r " +
                        "LEFT JOIN FETCH r.detallesRenta dr " +
                        "LEFT JOIN FETCH dr.idarticulo " +
                        "WHERE r.estado != 'SOLICITADA' " +
                        "ORDER BY r.id", Renta.class)
                .getResultList();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Metodo para obtener las rentas disponibles (sin asignar) y las asignadas al empleado logueado.
     * Utiliza LEFT JOIN FETCH para cargar cliente, empleado y detalles en una sola consulta.
     * Filtra por estados: 'Pendiente' para rentas libres y 'En proceso' para las propias.
     * @Throws Si la base de datos rechaza la consulta.
     * @Params Objeto de tipo Integer idEmpleadoLogueado
     * @return Una lista de objetos Renta ordenados por fecha y hora, o null si no se encuentran resultados.
     */
    public List<Renta> obtenerRentasDisponiblesYAsignadas(Integer idEmpleadoLogueado) {
        try {
            return entityManager.createQuery("SELECT r FROM Renta r " +
                            "LEFT JOIN FETCH r.detallesRenta dr " +
                            "LEFT JOIN FETCH dr.idarticulo " +
                            "LEFT JOIN FETCH r.idCliente " +
                            "LEFT JOIN FETCH r.idEmpleado " +
                            "WHERE " +
                            // CASO 1: Pendiente a REPARTO (Solo si no tiene dueño)
                            "(r.estado = 'Pendiente a reparto' AND r.idEmpleado IS NULL) " +
                            "OR " +
                            // CASO 2: Pendiente a RECOLECCION (Sale siempre pública para todos)
                            "(r.estado = 'Pendiente a recoleccion') " +
                            "OR " +
                            // CASO 3: Mis tareas activas (Lo que ya estoy haciendo)
                            "(r.idEmpleado.id = :empId AND r.estado IN ('En reparto', 'En recoleccion')) " +
                            "ORDER BY r.fecha ASC, r.hora ASC", Renta.class)
                    .setParameter("empId", idEmpleadoLogueado)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}