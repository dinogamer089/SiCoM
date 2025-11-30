package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Cliente;
import mx.desarollo.entity.Comentario;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public class RentaDAO extends AbstractDAO<Renta> {
    private final EntityManager entityManager;

    public RentaDAO(EntityManager em) {
        super(Renta.class);
        this.entityManager = em;
    }

    public List<Renta> obtenerTodosCotizaciones() {
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

    public List<Renta> obtenerTodosRentas() {
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
     * Verifica si un empleado tiene asignadas rentas en reparto o recoleccion.
     * @param empleadoId ID del empleado a validar.
     * @return true si existe al menos una renta en esos estados, false en otro caso.
     */
    public boolean existeAsignacionPendiente(Integer empleadoId) {
        if (empleadoId == null) {
            return false;
        }

        Long total = entityManager.createQuery(
                        "SELECT COUNT(r) FROM Renta r " +
                                "WHERE r.idEmpleado.id = :empId " +
                                "AND r.estado IN ('En reparto', 'En recoleccion')",
                        Long.class)
                .setParameter("empId", empleadoId)
                .getSingleResult();

        return total != null && total > 0;
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

    // Registro de renta/cotización con detalles y cliente asociado
    public void registrarRenta(Cliente cliente,
                               List<Detallerenta> detalles,
                               LocalDate fecha,
                               LocalTime hora,
                               String estado) {
        try {
            entityManager.getTransaction().begin();

            // Persistir/adjuntar cliente
            if (cliente != null) {
                if (cliente.getId() == null) {
                    entityManager.persist(cliente);
                    entityManager.flush();
                } else {
                    cliente = entityManager.merge(cliente);
                }
            }

            // Crear renta
            Renta renta = new Renta();
            renta.setEstado(estado != null ? estado : "SOLICITADA");
            renta.setIdCliente(cliente);
            renta.setFecha(fecha);
            renta.setHora(hora);

            // Calcular total a partir de los detalles
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            if (detalles != null) {
                for (Detallerenta d : detalles) {
                    if (d == null || d.getIdarticulo() == null || d.getCantidad() == null) continue;
                    java.math.BigDecimal pu = d.getIdarticulo().getPrecio();
                    if (pu != null) {
                        total = total.add(pu.multiply(java.math.BigDecimal.valueOf(d.getCantidad())));
                    }
                }
            }
            renta.setTotal(total);
            entityManager.persist(renta);
            entityManager.flush();

            // Persistir detalles
            if (detalles != null) {
                for (Detallerenta d : detalles) {
                    if (d == null) continue;
                    d.setIdrenta(renta);
                    if (d.getCantidad() == null) {
                        d.setCantidad(1);
                    }
                    java.math.BigDecimal pu = (d.getPrecioUnitario() != null)
                            ? d.getPrecioUnitario()
                            : (d.getIdarticulo() != null ? d.getIdarticulo().getPrecio() : java.math.BigDecimal.ZERO);
                    if (pu == null) pu = java.math.BigDecimal.ZERO;
                    d.setPrecioUnitario(pu);
                    java.math.BigDecimal pt = pu.multiply(java.math.BigDecimal.valueOf(d.getCantidad()));
                    d.setPrecioTotal(pt);

                    entityManager.persist(d);
                }
            }

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Obtiene la lista de comentarios asociados a una renta especifica.
     * @param idRenta El ID de la renta a consultar.
     * @return Lista de objetos Comentario.
     */
    public List<Comentario> obtenerComentarios(Integer idRenta) {
        if (idRenta == null) {
            return Collections.emptyList();
        }

        try {
            return entityManager.createQuery(
                            "SELECT c FROM Comentario c WHERE c.idRenta.id = :idRenta", Comentario.class)
                    .setParameter("idRenta", idRenta)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void guardarComentario(Comentario comentario) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(comentario);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
        }
    }
}

