package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import mx.avanti.desarollo.persistence.AbstractDAO;
import mx.avanti.desarollo.persistence.HibernateUtil;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;

import java.util.List;

public class ArticuloDAO extends AbstractDAO<Articulo> {

    private final EntityManager em;

    /**
     * Constructor que recibe un EntityManager para operar sobre Articulo.
     * @Throws Si el EntityManager es nulo o invalido.
     * @Params Objeto de tipo EntityManager em
     */
    public ArticuloDAO(EntityManager em) {
        super(Articulo.class);
        this.em = em;
    }

    /**
     * Constructor por defecto que obtiene el EntityManager desde HibernateUtil.
     * Se usa principalmente para el catalogo.
     * @Throws Si no se puede obtener un EntityManager valido.
     */
    public ArticuloDAO() {
        super(Articulo.class);
        this.em = HibernateUtil.getEntityManager();
    }

    /**
     * Metodo protegido para obtener el EntityManager utilizado por el DAO.
     * @return El EntityManager interno.
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Metodo para listar los articulos activos con stock y su imagen precargada.
     * Usado para mostrar el catalogo al cliente.
     * @Throws Si la base de datos rechaza la consulta o hay un error al ejecutar la query.
     * @return Una lista de articulos activos, con unidades > 0, ordenados por categoria y nombre.
     */
    public List<Articulo> listarActivosConStock() {
        final String jpql =
                "SELECT a FROM Articulo a " +
                        "LEFT JOIN FETCH a.imagen " +
                        "WHERE a.activo = true AND a.unidades > 0 " +
                        "ORDER BY a.categoria, a.nombre";
        return execute(e -> e.createQuery(jpql, Articulo.class).getResultList());
    }

    /**
     * Metodo para obtener todos los articulos con su imagen, ordenados por ID.
     * Usado principalmente en la administracion de articulos.
     * @Throws Si la base de datos rechaza la consulta o falla la transaccion.
     * @return Una lista con todos los articulos encontrados.
     */
    public List<Articulo> obtenerTodos() {
        return execute(em ->
                em.createQuery("SELECT DISTINCT a FROM Articulo a LEFT JOIN FETCH a.imagen ORDER BY a.id", Articulo.class)
                        .getResultList()
        );
    }

    /**
     * Obtiene un artículo por id con su imagen precargada (JOIN FETCH) para evitar LazyInitialization en la vista.
     * @param id identificador del artículo
     * @return Optional con el artículo o vacío si no existe
     */
    public java.util.Optional<Articulo> findWithImage(Integer id) {
        final String jpql = "SELECT a FROM Articulo a LEFT JOIN FETCH a.imagen WHERE a.id = :id";
        return java.util.Optional.ofNullable(
                execute(em -> em.createQuery(jpql, Articulo.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null))
        );
    }

    /**
     * Metodo para guardar un articulo junto con su imagen en una sola transaccion.
     * Primero persiste la imagen, luego enlaza la imagen al articulo y persiste el articulo.
     * @Throws Si la base de datos rechaza la operacion de guardado o hay un error de persistencia.
     * @Params Objetos de tipo Articulo articulo, Imagen imagen
     */
    public void saveWithImage(Articulo articulo, Imagen imagen) {
        execute(e -> {
            e.persist(imagen);
            e.flush();
            articulo.setImagen(imagen);
            e.persist(articulo);
            return null;
        });
    }

    /**
     * Metodo para eliminar un articulo por su ID dentro de una transaccion.
     * @Throws Si la base de datos rechaza la eliminacion o el articulo esta referenciado.
     * @Params Objeto de tipo Integer id
     */
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
