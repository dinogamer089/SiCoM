package helper;

import helper.dto.ArticuloCardDTO;
import mx.avanti.desarollo.dao.StockReservadoDAO;
import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Articulo;
import mx.desarollo.entity.Imagen;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class ArticuloHelper {

    /**
     * Convierte entidades a dto's calculando el stock real basado en la fecha.
     * @param entidades Lista de articulos base.
     * @param fecha Fecha del evento.
     * @return Lista de DTOs con stock ajustado.
     */
    public static List<ArticuloCardDTO> toCardDTOs(List<Articulo> entidades, LocalDate fecha) {
        List<ArticuloCardDTO> out = new ArrayList<>();
        if (entidades == null) return out;

        // Obtenemos instancia del StockReservadoDAO para consultar reservas
        StockReservadoDAO stockDAO = ServiceLocator.getInstanceStockReservadoDAO();

        for (Articulo a : entidades) {
            ArticuloCardDTO dto = new ArticuloCardDTO();

            dto.setId(a.getId());
            dto.setNombre(a.getNombre());
            dto.setPrecio(a.getPrecio());

            // 1 Stock total fisico
            int stockTotal = (a.getUnidades() != null) ? a.getUnidades() : 0;

            // 2 Calcular Disponibilidad Real
            int stockFinal = 0;

            if (fecha != null) {
                // Consultamos cuanto hay reservado para esa fecha especifica
                int reservado = stockDAO.obtenerReservado(a.getId(), fecha);
                // La disponibilidad es Total - Reservado
                stockFinal = Math.max(0, stockTotal - reservado);
            } else {
                // Si no hay fecha seleccionada, mostramos 0 o el total
                stockFinal = 0;
            }

            dto.setUnidades(stockFinal);
            // Disponible si hay mas de 0 unidades libres ese dia
            dto.setDisponible(stockFinal > 0);

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

    public void actualizar(Articulo articulo) {
        ServiceFacadeLocator.getInstanceFacadeArticulo().actualizarArticulo(articulo);
    }
}