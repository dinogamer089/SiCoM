package helper;

import mx.desarollo.entity.CombinacionMesa;
import mx.desarollo.facade.FacadeCombinacionMesa;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.util.List;
import java.util.Optional;

public class CombinacionMesaHelper {

    private FacadeCombinacionMesa facade() {
        return ServiceFacadeLocator.getInstanceFacadeCombinacionMesa();
    }

    public List<CombinacionMesa> obtenerTodas() {
        return facade().obtenerTodas();
    }

    public Optional<CombinacionMesa> buscarPorMesaYTextiles(Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre) {
        return facade().obtenerPorMesaYTextiles(idMesa, idMantel, idCamino, idCubre);
    }

    public void guardar(CombinacionMesa combinacion) {
        facade().crearOActualizar(combinacion);
    }

    public void eliminarPorId(Integer id) {
        facade().eliminar(id);
    }

    public String toDataUrl(CombinacionMesa c) {
        if (c == null || c.getImagen() == null || c.getImagen().getDatos() == null) return null;
        return ImagenHelper.toDataUrl(c.getImagen().getMime(), c.getImagen().getDatos());
    }
}

