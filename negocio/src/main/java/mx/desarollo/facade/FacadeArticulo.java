package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateArticulo;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;

import java.util.List;
import java.util.Locale;

public class FacadeArticulo {

    private final DelegateArticulo delegateArticulo;

    /**
     * Constructor por defecto que inicializa el delegate de articulo.
     * @Throws Si ocurre un error al crear la instancia del delegate.
     */
    public FacadeArticulo() {
        this.delegateArticulo = new DelegateArticulo();
    }

    /**
     * Metodo para listar los articulos que se muestran en el catalogo del cliente.
     * Solo incluye articulos activos con stock disponible.
     * @Throws Si la base de datos rechaza la consulta o hay error en el DAO.
     * @return Una lista de articulos para mostrar en el catalogo.
     */
    public List<Articulo> listarCatalogoCliente() {
        return delegateArticulo.listarCatalogoCliente();
    }

    /**
     * Metodo para obtener todos los articulos registrados (para alta/administracion).
     * @Throws Si la base de datos rechaza la consulta o hay error en la capa de datos.
     * @return Una lista con todos los articulos encontrados.
     */
    public List<Articulo> obtenerArticulos() {
        return delegateArticulo.findAllArticulos();
    }

    /**
     * Metodo para crear un articulo con su imagen asociada en una sola transaccion.
     * @Throws Si la base de datos rechaza la operacion de guardado o hay error de persistencia.
     * @Params Objetos de tipo Articulo articulo, Imagen imagen
     */
    public void crearArticuloConImagen(Articulo articulo, Imagen imagen) {
        if (articulo == null) {
            throw new IllegalArgumentException("El articulo no puede ser nulo");
        }
        String nombre = articulo.getNombre();
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del articulo no puede estar vacio");
        }
        // Validación de duplicados por nombre (trim + lower)
        if (delegateArticulo.existsArticuloByNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe un articulo con ese nombre");
        }
        articulo.setNombre(nombre.trim());
        delegateArticulo.saveArticuloWithImage(articulo, imagen);
    }

    /**
     * Metodo para eliminar un articulo por su ID desde la capa fachada.
     * @Throws Si la base de datos rechaza la eliminacion o el articulo esta referenciado.
     * @Params Objeto de tipo Integer id
     */
    public void eliminarArticuloPorId(Integer id) {
        delegateArticulo.deleteArticuloById(id);
    }

    /**
     * Metodo para obtener un articulo por su ID desde la capa fachada.
     * @Throws Si la base de datos rechaza la busqueda o la consulta falla.
     * @Params Objeto de tipo Integer id
     * @return Un Optional con el articulo encontrado o vacio si no existe.
     */
    public java.util.Optional<Articulo> obtenerArticuloPorId(Integer id) {
        return delegateArticulo.findById(id);
    }

    /**
     * Variante para UI que necesita la imagen precargada para evitar LazyInitialization en JSF.
     */
    public java.util.Optional<Articulo> obtenerArticuloConImagenPorId(Integer id) {
        return delegateArticulo.findByIdWithImage(id);
    }

    public void actualizarArticulo(Articulo articulo) {
        if (articulo == null || articulo.getId() == null) {
            throw new IllegalArgumentException("El ID del articulo es requerido para actualizar");
        }
        String nombre = articulo.getNombre();
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del articulo no puede estar vacio");
        }
        if (delegateArticulo.existsArticuloByNombreExcludingId(nombre, articulo.getId())) {
            throw new IllegalArgumentException("Ya existe un articulo con ese nombre");
        }
        articulo.setNombre(nombre.trim());
        delegateArticulo.updateArticulo(articulo);
    }
}
