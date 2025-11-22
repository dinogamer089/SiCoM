package helper;

import mx.desarollo.entity.CombinacionMesa;
import mx.desarollo.facade.FacadeCombinacionMesa;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.util.List;
import java.util.Optional;

public class CombinacionMesaHelper {

    /**
     * Metodo privado para obtener la instancia de FacadeCombinacionMesa
     * a traves del ServiceFacadeLocator.
     * @return Una instancia de FacadeCombinacionMesa.
     */
    private FacadeCombinacionMesa facade() {
        return ServiceFacadeLocator.getInstanceFacadeCombinacionMesa();
    }

    /**
     * Metodo para obtener todas las combinaciones de mesa existentes.
     * @Throws Si la base de datos rechaza la consulta en la capa fachada/DAO.
     * @return Una lista con todas las combinaciones de mesa.
     */
    public List<CombinacionMesa> obtenerTodas() {
        return facade().obtenerTodas();
    }

    /**
     * Metodo para buscar una combinacion de mesa y textiles por sus IDs.
     * @Throws Si la base de datos rechaza la consulta o falla la busqueda.
     * @Params Objetos de tipo Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre
     * @return Un Optional con la combinacion encontrada o vacio si no existe.
     */
    public Optional<CombinacionMesa> buscarPorMesaYTextiles(Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre) {
        return facade().obtenerPorMesaYTextiles(idMesa, idMantel, idCamino, idCubre);
    }

    /**
     * Metodo para guardar una combinacion de mesa nueva o actualizada.
     * Delegado a la fachada que contiene las reglas de negocio.
     * @Throws Si los datos no cumplen las reglas de negocio o la base de datos rechaza la operacion.
     * @Params Objeto de tipo CombinacionMesa combinacion
     */
    public void guardar(CombinacionMesa combinacion) {
        facade().crearOActualizar(combinacion);
    }

    /**
     * Metodo para eliminar una combinacion de mesa por su ID desde el helper.
     * @Throws Si la base de datos rechaza la eliminacion o la combinacion esta referenciada.
     * @Params Objeto de tipo Integer id
     */
    public void eliminarPorId(Integer id) {
        facade().eliminar(id);
    }

    /**
     * Metodo para generar una representacion en Data URL (base64) de la imagen
     * asociada a una combinacion de mesa, lista para usarse en <img src="...">.
     * @Throws Si ocurre un error al convertir la imagen a Data URL.
     * @Params Objeto de tipo CombinacionMesa c
     * @return Una cadena con el Data URL de la imagen o null si no hay imagen.
     */
    public String toDataUrl(CombinacionMesa c) {
        if (c == null || c.getImagen() == null || c.getImagen().getDatos() == null) return null;
        return ImagenHelper.toDataUrl(c.getImagen().getMime(), c.getImagen().getDatos());
    }
}
