package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.avanti.desarollo.persistence.HibernateUtil;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;

import java.util.List;

public class ArticuloDAO extends AbstractDAO<Articulo> {

    private final EntityManager em;

    // Constructor
    public ArticuloDAO(EntityManager em) {
        super(Articulo.class);
        this.em = em;
    }

    // Constructor por defecto se usa en Catalogo
    public ArticuloDAO() {
        super(Articulo.class);
        this.em = HibernateUtil.getEntityManager();
    }

    public void saveWithImage(Articulo articulo, Imagen imagen) {
        execute(em -> {
            em.persist(imagen);
            em.flush();
            articulo.setImagen(imagen);
            em.persist(articulo);
            return null;
        });
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /** Catalogo: activos con stock + imagen precargada */
    public List<Articulo> listarActivosConStock() {
        final String jpql =
                "SELECT a FROM Articulo a " +
                        "LEFT JOIN FETCH a.imagen " +
                        "WHERE a.activo = true AND a.unidades > 0 " +
                        "ORDER BY a.categoria, a.nombre";
        return execute(e -> e.createQuery(jpql, Articulo.class).getResultList());
    }

    /** Alta: obtener todos */
    public List<Articulo> obtenerTodos() {
        return execute(em ->
                em.createQuery("SELECT DISTINCT a FROM Articulo a LEFT JOIN FETCH a.imagen ORDER BY a.id", Articulo.class)
                        .getResultList()
        );
    }


    /** Alta: persistir imagen y articulo en una sola transacción */
    public void saveWithImage(Articulo articulo, Imagen imagen) {
        execute(e -> {
            e.persist(imagen);
            e.flush();
            articulo.setImagen(imagen);
            e.persist(articulo);
            return null;
        });
    }

    /** Eliminar por ID (con transacción) */
    public void deleteById(Integer id) {
        execute(e -> {
            Articulo a = e.find(Articulo.class, id);
            if (a != null) {
                e.remove(a);
            }
            return null;
        });
    }
}
