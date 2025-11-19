package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.CombinacionMesa;

import java.util.List;
import java.util.Optional;

public class DelegateCombinacionMesa {

    public Optional<CombinacionMesa> buscarPorMesaYTextiles(Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre) {
        return ServiceLocator.getInstanceCombinacionMesaDAO()
                .buscarPorMesaYTextiles(idMesa, idMantel, idCamino, idCubre);
    }

    public List<CombinacionMesa> listarTodas() {
        return ServiceLocator.getInstanceCombinacionMesaDAO().listarTodas();
    }

    public void guardar(CombinacionMesa combinacion) {
        ServiceLocator.getInstanceCombinacionMesaDAO().saveWithImage(combinacion);
    }

    public void eliminarPorId(Integer id) {
        ServiceLocator.getInstanceCombinacionMesaDAO().deleteById(id);
    }
}

