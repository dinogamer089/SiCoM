package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateCombinacionMesa;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Categoria;
import mx.desarollo.entity.CombinacionMesa;
import mx.desarollo.entity.Forma;
import mx.desarollo.entity.TextilTipo;

import java.util.List;
import java.util.Optional;

public class FacadeCombinacionMesa {

    private final DelegateCombinacionMesa delegate = new DelegateCombinacionMesa();

    public Optional<CombinacionMesa> obtenerPorMesaYTextiles(Integer idMesa, Integer idMantel, Integer idCamino, Integer idCubre) {
        return delegate.buscarPorMesaYTextiles(idMesa, idMantel, idCamino, idCubre);
    }

    public List<CombinacionMesa> obtenerTodas() {
        return delegate.listarTodas();
    }

    public void crearOActualizar(CombinacionMesa c) {
        // Validaciones de negocio
        if (c.getMesa() == null || c.getMantel() == null || c.getImagen() == null) {
            throw new IllegalArgumentException("Mesa, mantel e imagen son obligatorios");
        }
        validarCategoriasYFormas(c.getMesa(), c.getMantel(), c.getCamino(), c.getCubre());

        Integer idMesa = c.getMesa().getId();
        Integer idMantel = c.getMantel().getId();
        Integer idCamino = (c.getCamino() != null) ? c.getCamino().getId() : null;
        Integer idCubre = (c.getCubre() != null) ? c.getCubre().getId() : null;

        // Anti duplicado
        Optional<CombinacionMesa> existente = obtenerPorMesaYTextiles(idMesa, idMantel, idCamino, idCubre);
        if (existente.isPresent() && (c.getId() == null || !existente.get().getId().equals(c.getId()))) {
            throw new IllegalStateException("Ya existe una combinación con los mismos artículos");
        }

        delegate.guardar(c);
    }

    public void eliminar(Integer id) {
        delegate.eliminarPorId(id);
    }

    private void validarCategoriasYFormas(Articulo mesa, Articulo mantel, Articulo camino, Articulo cubre) {
        if (mesa.getCategoria() != Categoria.MESA) {
            throw new IllegalArgumentException("La mesa debe tener categoria MESA");
        }
        if (mantel.getCategoria() != Categoria.TEXTIL || mantel.getTextilTipo() != TextilTipo.MANTEL) {
            throw new IllegalArgumentException("El mantel debe ser TEXTIL de tipo MANTEL");
        }
        Forma formaMesa = mesa.getForma();
        if (formaMesa == null || mantel.getForma() != formaMesa) {
            throw new IllegalArgumentException("La forma del mantel debe coincidir con la mesa");
        }
        if (camino != null) {
            if (camino.getCategoria() != Categoria.TEXTIL || camino.getTextilTipo() != TextilTipo.CAMINO) {
                throw new IllegalArgumentException("El camino debe ser TEXTIL de tipo CAMINO");
            }
            // Caminos: misma forma O UNIVERSAL
            if (camino.getForma() != formaMesa && camino.getForma() != Forma.UNIVERSAL) {
                throw new IllegalArgumentException("La forma del camino debe coincidir con la mesa o ser UNIVERSAL");
            }
        }
        if (cubre != null) {
            if (cubre.getCategoria() != Categoria.TEXTIL || cubre.getTextilTipo() != TextilTipo.CUBRE) {
                throw new IllegalArgumentException("El cubremantel debe ser TEXTIL de tipo CUBRE");
            }
            // Cubres: misma forma O UNIVERSAL
            if (cubre.getForma() != formaMesa && cubre.getForma() != Forma.UNIVERSAL) {
                throw new IllegalArgumentException("La forma del cubremantel debe coincidir con la mesa o ser UNIVERSAL");
            }
        }
    }
}
