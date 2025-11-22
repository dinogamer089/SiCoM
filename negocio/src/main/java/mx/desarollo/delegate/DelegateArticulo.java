package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;

import java.util.List;

public class DelegateArticulo {

    /**
     * Metodo para listar los articulos activos con stock disponibles para el catalogo del cliente.
     * Llama al ArticuloDAO para obtener solo los articulos visibles en el catalogo.
     * @Throws Si la base de datos rechaza la peticion de listado o la consulta falla.
     * @return Una lista de articulos activos con stock y su imagen precargada.
     */
    public List<Articulo> listarCatalogoCliente() {
        return ServiceLocator.getInstanceArticuloDAO().listarActivosConStock();
    }

    /**
     * Metodo para obtener todos los articulos registrados sin filtros.
     * @Throws Si la base de datos rechaza la peticion de listado o la transaccion falla.
     * @return Una lista con todos los articulos encontrados.
     */
    public List<Articulo> findAllArticulos() {
        return ServiceLocator.getInstanceArticuloDAO().obtenerTodos();
    }

    /**
     * Metodo para guardar un articulo junto con su imagen asociada en una sola transaccion.
     * @Throws Si la base de datos rechaza la peticion de guardado o ocurre un error de persistencia.
     * @Params Objetos de tipo Articulo articulo, Imagen imagen
     */
    public void saveArticuloWithImage(Articulo articulo, Imagen imagen) {
        ServiceLocator.getInstanceArticuloDAO().saveWithImage(articulo, imagen);
    }

    /**
     * Metodo para eliminar un articulo por su ID.
     * Primero busca el articulo y solo lo elimina si existe.
     * @Throws Si la base de datos rechaza la peticion de eliminacion o la transaccion falla.
     * @Params Objeto de tipo Integer id
     */
    public void deleteArticuloById(Integer id) {
        var dao = ServiceLocator.getInstanceArticuloDAO();
        dao.find(id).ifPresent(dao::delete);
    }

    /**
     * Metodo para obtener un articulo por su ID.
     * Llama al DAO para realizar la busqueda.
     * @Throws Si la base de datos rechaza la peticion de busqueda o la consulta falla.
     * @Params Objeto de tipo Integer id
     * @return Un Optional con el articulo encontrado o vacio si no existe.
     */
    public java.util.Optional<Articulo> findById(Integer id) {
        return ServiceLocator.getInstanceArticuloDAO().find(id);
    }
}
