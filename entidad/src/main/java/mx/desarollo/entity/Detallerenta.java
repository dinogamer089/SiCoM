package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "detallerenta")
public class Detallerenta implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalle", nullable = false)
    private Integer iddetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idrenta", referencedColumnName = "idrenta", nullable = false)
    private Renta renta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idarticulo", referencedColumnName = "idarticulo", nullable = false)
    private Articulo articulo;

    @NotNull
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @NotNull
    @Digits(integer = 8, fraction = 2)
    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @NotNull
    @Digits(integer = 8, fraction = 2)
    @Column(name = "precio_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioTotal;

    @PrePersist @PreUpdate

    public void calcularTotal() {
        if (precioUnitario != null && cantidad != null && cantidad >= 0) {
            this.precioTotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        } else if (this.precioTotal == null) {
            this.precioTotal = BigDecimal.ZERO;
        }
    }

    // getters/setters
    public Integer getIddetalle() { return iddetalle; }
    public void setIddetalle(Integer iddetalle) { this.iddetalle = iddetalle; }
    public Renta getRenta() { return renta; }
    public void setRenta(Renta renta) { this.renta = renta; }
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getPrecioTotal() {
        if (this.precioTotal == null) {
            calcularTotal();
        }
        return precioTotal;
    }
    public void setPrecioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; }
}