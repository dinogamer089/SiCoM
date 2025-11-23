package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Cliente;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

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
                    // Asegurar precios requeridos por el esquema
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
}
