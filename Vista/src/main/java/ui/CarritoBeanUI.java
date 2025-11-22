package ui;

import helper.dto.ArticuloCardDTO;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Bean de sesion que guarda el carrito
 * En esta US solo agregar
 */
@Named("carritoUI")
@SessionScoped
public class CarritoBeanUI implements Serializable {

    private final Carrito carrito = new Carrito();

    public void agregar(ArticuloCardDTO dto) {
        carrito.agregar(dto.getId(), dto.getNombre(), dto.getPrecio());
    }
}
