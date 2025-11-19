package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.avanti.desarollo.persistence.HibernateUtil;
import mx.desarollo.entity.CombinacionMesa;

import java.util.List;
import java.util.Optional;

public class CombinacionMesaDAO extends AbstractDAO<CombinacionMesa> {

    private final EntityManager em;

    public CombinacionMesaDAO(EntityManager em) {
        super(CombinacionMesa.class);
        this.em = em;
    }

    public CombinacionMesaDAO() {
        super(CombinacionMesa.class);
        this.em = HibernateUtil.getEntityManager();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public Optional<CombinacionMesa> buscarPorMesaYTextiles(Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT c FROM CombinacionMesa c ")
                .append("JOIN FETCH c.imagen img ")
                .append("JOIN FETCH c.mesa m ")
                .append("JOIN FETCH c.mantel mt ");
        jpql.append("WHERE m.id = :idMesa AND mt.id = :idMantel ");
        if (idCamino == null) {
            jpql.append("AND c.camino IS NULL ");
        } else {
            jpql.append("AND c.camino.id = :idCamino ");
        }
        if (idCubre == null) {
            jpql.append("AND c.cubre IS NULL ");
        } else {
            jpql.append("AND c.cubre.id = :idCubre ");
        }

        return execute(em -> {
            try {
                var q = em.createQuery(jpql.toString(), CombinacionMesa.class)
                        .setParameter("idMesa", idMesa)
                        .setParameter("idMantel", idMantel);
                if (idCamino != null) q.setParameter("idCamino", idCamino);
                if (idCubre != null) q.setParameter("idCubre", idCubre);
                return Optional.ofNullable(q.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });
    }

    public List<CombinacionMesa> listarTodas() {
        final String jpql = "SELECT c FROM CombinacionMesa c " +
                "LEFT JOIN FETCH c.mesa " +
                "LEFT JOIN FETCH c.mantel " +
                "LEFT JOIN FETCH c.camino " +
                "LEFT JOIN FETCH c.cubre " +
                "LEFT JOIN FETCH c.imagen " +
                "ORDER BY c.id";
        return execute(em -> em.createQuery(jpql, CombinacionMesa.class).getResultList());
    }

    public void saveWithImage(CombinacionMesa combo) {
        execute(e -> {
            if (combo.getImagen() != null && combo.getImagen().getId() == null) {
                e.persist(combo.getImagen());
                e.flush();
            }
            combo.setMesa(e.getReference(mx.desarollo.entity.Articulo.class, combo.getMesa().getId()));
            combo.setMantel(e.getReference(mx.desarollo.entity.Articulo.class, combo.getMantel().getId()));
            if (combo.getCamino() != null) {
                combo.setCamino(e.getReference(mx.desarollo.entity.Articulo.class, combo.getCamino().getId()));
            }
            if (combo.getCubre() != null) {
                combo.setCubre(e.getReference(mx.desarollo.entity.Articulo.class, combo.getCubre().getId()));
            }
            e.persist(combo);
            return null;
        });
    }

    public void deleteById(Integer id) {
        execute(e -> {
            // Obtener id de imagen asociado antes de eliminar la combinacion
            Long imagenId = null;
            try {
                imagenId = e.createQuery("SELECT c.imagen.id FROM CombinacionMesa c WHERE c.id = :id", Long.class)
                        .setParameter("id", id)
                        .getSingleResult();
            } catch (NoResultException ignore) { }

            int deleted = e.createQuery("DELETE FROM CombinacionMesa c WHERE c.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            e.flush();

            // Si había imagen asociada y se eliminó la combinación, eliminar imagen
            if (deleted > 0 && imagenId != null) {
                var refImg = e.find(mx.desarollo.entity.Imagen.class, imagenId);
                if (refImg != null) {
                    e.remove(refImg);
                }
            }
            return null;
        });
    }
}
