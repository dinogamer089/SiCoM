package helper;

import helper.dto.ArticuloCardDTO;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class ArticuloHelper {

    /* ======= Catalogo: mapeo a DTO ======= */
    public static List<ArticuloCardDTO> toCardDTOs(List<Articulo> entidades) {
        List<ArticuloCardDTO> out = new ArrayList<>();
        if (entidades == null) return out;

        for (Articulo a : entidades) {
            ArticuloCardDTO dto = new ArticuloCardDTO();

            dto.setId(a.getId());
            dto.setNombre(a.getNombre());
            dto.setPrecio(a.getPrecio());

            int stock = (a.getUnidades() != null) ? a.getUnidades() : 0;
            dto.setUnidades(stock);
            dto.setDisponible(stock > 0);

            dto.setCategoria(a.getCategoria());
            dto.setForma(a.getForma());
            dto.setTextilTipo(a.getTextilTipo());

            if (a.getImagen() != null && a.getImagen().getDatos() != null) {
                String mime = a.getImagen().getMime();
                String b64 = Base64.getEncoder().encodeToString(a.getImagen().getDatos());
                dto.setImagenDataUrl("data:" + mime + ";base64," + b64);
            }

            out.add(dto);
        }
        return out;
    }

    /* ======= Alta ======= */
    public List<Articulo> obtenerTodas() {
        return ServiceFacadeLocator.getInstanceFacadeArticulo().obtenerArticulos();
    }

    public void guardarConImagen(Articulo articulo, Imagen imagen) {
        ServiceFacadeLocator.getInstanceFacadeArticulo().crearArticuloConImagen(articulo, imagen);
    }

    /* ======= Eliminación ======= */
    public void eliminarPorId(Integer id) {
        ServiceFacadeLocator.getInstanceFacadeArticulo().eliminarArticuloPorId(id);
    }
}
