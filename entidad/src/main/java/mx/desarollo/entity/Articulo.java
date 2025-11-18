package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "articulo")
public class Articulo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idarticulo", nullable = false)
    private Integer idarticulo;

    @NotBlank
    @Size(max = 45)
    @Column(name = "nombre", length = 45, nullable = false)
    private String nombre;

    // DECIMAL(10,2)
    @NotNull
    @Digits(integer = 8, fraction = 2)
    @Column(name = "precio", precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;

    @NotNull
    @Column(name = "unidades", nullable = false)
    private Integer unidades;

    @Size(max = 45)
    @Column(name = "tipo", length = 45)
    private String tipo;

    @Lob
    // SOLUCIÓN: Especificar el tipo de columna exacto de la BD
    @Column(name = "imagen", columnDefinition="longblob")
    private byte[] imagen;

    // getters/setters
    public Integer getIdarticulo() { return idarticulo; }
    public void setIdarticulo(Integer idarticulo) { this.idarticulo = idarticulo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getUnidades() { return unidades; }
    public void setUnidades(Integer unidades) { this.unidades = unidades; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public byte[] getImagen() { return imagen; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }
}
