package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.desarollo.entity.Cliente;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;
import mx.desarollo.entity.Articulo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// DAO para gestionar operaciones de la entidad Renta
public class RentaDAO extends AbstractDAO<Renta> {

    private final EntityManager entityManager;

    // Constructor que inicializa el EntityManager
    public RentaDAO(EntityManager em) {
        super(Renta.class);
        this.entityManager = em;
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    // Registra una renta completa con cliente, detalles y fecha/hora
    public Renta registrarRentaCompleta(
            Cliente cliente,
            List<Detallerenta> detalles,
            LocalDate fecha,
            LocalTime hora,
            String estado
    ) {
        return execute(em -> {
            System.out.println("[RentaDAO] registrarRentaCompleta() fecha=" + fecha + ", hora=" + hora + ", estado=" + estado);
            // Gestiona el cliente (persiste si es nuevo, mezcla si existe)
            Cliente clienteEntity;
            if (cliente.getIdCliente() == null || cliente.getIdCliente() == 0) {
                em.persist(cliente);
                em.flush();
                clienteEntity = cliente;
            } else {
                clienteEntity = em.merge(cliente);
            }

            // Crea y configura la renta
            Renta renta = new Renta();
            renta.setFecha(fecha);
            renta.setHora(hora);
            renta.setCliente(clienteEntity);

            // Calcula total y añade detalles
            BigDecimal totalRenta = BigDecimal.ZERO;
            for (Detallerenta det : detalles) {
                if (det.getArticulo() != null) {
                    det.setArticulo(em.merge(det.getArticulo()));
                }
                det.calcularTotal();
                totalRenta = totalRenta.add(det.getPrecioTotal());
                renta.addDetalle(det);
            }
            renta.setTotal(totalRenta);
            renta.setEstado(estado);

            if (renta.getEstado() == null || renta.getEstado().isBlank()) {
                renta.setEstado("PENDIENTE");
            }
            em.persist(renta);
            em.flush();
            System.out.println("[RentaDAO] Renta creada id=" + renta.getIdrenta() + ", total=" + renta.getTotal());
            return renta;
        });
    }

    // Guarda una renta pendiente (carrito) con cliente actual y detalles
    public Renta guardarCarrito(Cliente cliente, List<Detallerenta> detalles) {
        return execute(em -> {
            // Gestiona el cliente
            Cliente clienteEntity;
            if (cliente.getIdCliente() == null) {
                em.persist(cliente);
                clienteEntity = cliente;
            } else {
                clienteEntity = em.merge(cliente);
            }

            // Configura renta actual
            Renta renta = new Renta();
            renta.setCliente(clienteEntity);
            renta.setFecha(java.time.LocalDate.now());
            renta.setHora(java.time.LocalTime.now());

            // Procesa detalles y calcula total
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (Detallerenta det : detalles) {
                if (det.getArticulo() != null && det.getArticulo().getIdarticulo() != null) {
                    det.setArticulo(em.getReference(Articulo.class, det.getArticulo().getIdarticulo()));
                }
                renta.addDetalle(det);
                det.calcularTotal();
                total = total.add(det.getPrecioTotal());
            }

            // Asigna total y persiste
            renta.setTotal(total);
            em.persist(renta);
            em.flush();
            return renta;
        });
    }
}
