package helper;

import helper.dto.ArticuloCardDTO;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class ArticuloHelper {

    /**
     * Metodo estatico para convertir una lista de entidades Articulo
     * a una lista de objetos ArticuloCardDTO para el catalogo.
     * Incluye informacion de stock, categoria, forma y una imagen en Data URL.
     * @Throws Si ocurre un error al convertir los datos de imagen a base64.
     * @Params Lista de objetos de tipo Articulo entidades
     * @return Una lista de objetos ArticuloCardDTO listos para usarse en la vista.
     */
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

    /**
     * Metodo para obtener todos los articulos desde la fachada de articulo.
     * Usado en pantallas de administracion/alta.
     * @Throws Si la base de datos rechaza la consulta o hay error en la capa fachada.
     * @return Una lista con todos los articulos existentes.
     */
    public List<Articulo> obtenerTodas() {
        return ServiceFacadeLocator.getInstanceFacadeArticulo().obtenerArticulos();
    }

    /**
     * Metodo para guardar un articulo junto con su imagen mediante la fachada.
     * @Throws Si la base de datos rechaza la operacion de guardado o falla la transaccion.
     * @Params Objetos de tipo Articulo articulo, Imagen imagen
     */
    public void guardarConImagen(Articulo articulo, Imagen imagen) {
        ServiceFacadeLocator.getInstanceFacadeArticulo().crearArticuloConImagen(articulo, imagen);
    }

    /**
     * Metodo para eliminar un articulo por su ID usando la fachada.
     * @Throws Si la base de datos rechaza la eliminacion o el articulo esta referenciado.
     * @Params Objeto de tipo Integer id
     */
    public void eliminarPorId(Integer id) {
        ServiceFacadeLocator.getInstanceFacadeArticulo().eliminarArticuloPorId(id);
    }
}
