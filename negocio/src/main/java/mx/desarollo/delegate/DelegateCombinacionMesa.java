package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.CombinacionMesa;

import java.util.List;
import java.util.Optional;

public class DelegateCombinacionMesa {

    /**
     * Metodo para buscar una combinacion de mesa y textiles por sus IDs.
     * Llama a la instancia de CombinacionMesaDAO a traves del ServiceLocator.
     * @Throws Si la base de datos rechaza la peticion de busqueda o hay un error en la capa DAO.
     * @Params Objeto de tipo Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre
     * @return Un Optional con la combinacion encontrada o vacio si no existe.
     */
    public Optional<CombinacionMesa> buscarPorMesaYTextiles(Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre) {
        return ServiceLocator.getInstanceCombinacionMesaDAO()
                .buscarPorMesaYTextiles(idMesa, idMantel, idCamino, idCubre);
    }

    /**
     * Metodo para listar todas las combinaciones de mesa registradas.
     * @Throws Si la base de datos rechaza la peticion de listado o hay un error en la consulta.
     * @return Una lista con todas las combinaciones de mesa encontradas.
     */
    public List<CombinacionMesa> listarTodas() {
        return ServiceLocator.getInstanceCombinacionMesaDAO().listarTodas();
    }

    /**
     * Metodo para guardar o actualizar una combinacion de mesa.
     * Llama al DAO para persistir la entidad junto con su imagen asociada.
     * @Throws Si la base de datos rechaza la peticion de guardado o la transaccion falla.
     * @Params Objeto de tipo CombinacionMesa combinacion
     */
    public void guardar(CombinacionMesa combinacion) {
        ServiceLocator.getInstanceCombinacionMesaDAO().saveWithImage(combinacion);
    }

    /**
     * Metodo para eliminar una combinacion de mesa por su ID.
     * @Throws Si la base de datos rechaza la peticion de eliminacion o no se encuentra el registro.
     * @Params Objeto de tipo Integer id
     */
    public void eliminarPorId(Integer id) {
        ServiceLocator.getInstanceCombinacionMesaDAO().deleteById(id);
    }
}
