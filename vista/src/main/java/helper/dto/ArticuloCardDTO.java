package helper.dto;

import mx.desarollo.entity.Categoria;
import mx.desarollo.entity.Forma;
import mx.desarollo.entity.TextilTipo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO que la vista usa para pintar las tarjetas de articulos
 */
public class ArticuloCardDTO implements Serializable {

    private Integer id;
    private String nombre;
    private BigDecimal precio;
    private boolean disponible;

    /** DataURL (base64) para mostrar la imagen en la vista; puede venir null */
    private String imagenDataUrl;

    private Categoria categoria;
    private Forma forma;
    private TextilTipo textilTipo;

    /** NUEVO: unidades disponibles (stock) */
    private int unidades;

    // ===== getters & setters =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getImagenDataUrl() { return imagenDataUrl; }
    public void setImagenDataUrl(String imagenDataUrl) { this.imagenDataUrl = imagenDataUrl; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Forma getForma() { return forma; }
    public void setForma(Forma forma) { this.forma = forma; }

    public TextilTipo getTextilTipo() { return textilTipo; }
    public void setTextilTipo(TextilTipo textilTipo) { this.textilTipo = textilTipo; }

    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) { this.unidades = unidades; }
}
