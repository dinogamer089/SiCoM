package mx.desarollo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Articulo del catalogo con inventario y FK 1:1 a Imagen
 * */
@Entity
@Table(name = "articulo")
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idarticulo")
    private Integer id;

    /** Nombre visible en el catalogo */
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    /** Clasificacion funcional del articulo */
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 10)
    private Categoria categoria;

    /** Precio unitario mostrado */
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Stock disponible para renta */
    @Column(name = "unidades", nullable = false)
    private Integer unidades;

    /** Visible en catalogo (true) o no (false) */
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    /** Imagen asociada (exactamente una por articulo) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "imagen_id", nullable = false, unique = true)
    private Imagen imagen;

    // --------- Campos opcionales para filtros/UX del catalogo ---------

    /** Para mesas (REDONDA/RECTANGULAR) o UNIVERSAL si aplica */
    @Enumerated(EnumType.STRING)
    @Column(name = "forma", length = 12)
    private Forma forma;

    /** Si es TEXTIL: MANTEL/CAMINO/CUBRE en otras categorías debe ser null */
    @Enumerated(EnumType.STRING)
    @Column(name = "textil_tipo", length = 10)
    private TextilTipo textilTipo;

    // Getters y Setters
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getUnidades() { return unidades; }
    public void setUnidades(Integer unidades) { this.unidades = unidades; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Imagen getImagen() { return imagen; }
    public void setImagen(Imagen imagen) { this.imagen = imagen; }
    public Forma getForma() { return forma; }
    public void setForma(Forma forma) { this.forma = forma; }
    public TextilTipo getTextilTipo() { return textilTipo; }
    public void setTextilTipo(TextilTipo textilTipo) { this.textilTipo = textilTipo; }
}
