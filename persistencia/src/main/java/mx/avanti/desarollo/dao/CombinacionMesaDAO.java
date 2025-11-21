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

    /**
     * Constructor que recibe un EntityManager para operar sobre CombinacionMesa.
     * @Throws Si el EntityManager proporcionado es nulo o invalido.
     * @Params Objeto de tipo EntityManager em
     */
    public CombinacionMesaDAO(EntityManager em) {
        super(CombinacionMesa.class);
        this.em = em;
    }

    /**
     * Constructor por defecto que obtiene el EntityManager desde HibernateUtil.
     * @Throws Si no se puede obtener un EntityManager valido.
     */
    public CombinacionMesaDAO() {
        super(CombinacionMesa.class);
        this.em = HibernateUtil.getEntityManager();
    }

    /**
     * Metodo protegido para obtener el EntityManager interno.
     * @return El EntityManager asociado al DAO.
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Metodo para buscar una combinacion de mesa y textiles por sus IDs usando JPQL.
     * Incluye fetch de imagen, mesa y manteles para evitar problemas de lazy loading.
     * @Throws Si la base de datos rechaza la consulta o hay error al ejecutar la query.
     * @Params Objetos de tipo Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre
     * @return Un Optional con la combinacion encontrada o vacio si no existe resultado.
     */
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

    /**
     * Metodo para listar todas las combinaciones de mesa con sus relaciones cargadas.
     * Hace LEFT JOIN FETCH con mesa, mantel, camino, cubre e imagen para optimizar las consultas.
     * @Throws Si la base de datos rechaza la consulta o falla la ejecucion de la query.
     * @return Una lista con todas las combinaciones de mesa ordenadas por ID.
     */
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

    /**
     * Metodo para guardar una combinacion de mesa junto con su imagen asociada.
     * Primero persiste la imagen si es nueva y luego referencia los articulos por ID.
     * @Throws Si la base de datos rechaza la transaccion o no se pueden persistir las entidades.
     * @Params Objeto de tipo CombinacionMesa combo
     */
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

    /**
     * Metodo para eliminar una combinacion de mesa por su ID.
     * Si la combinacion tiene una imagen asociada, tambien se elimina la imagen.
     * @Throws Si la base de datos rechaza la eliminacion o la transaccion falla.
     * @Params Objeto de tipo Integer id
     */
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
