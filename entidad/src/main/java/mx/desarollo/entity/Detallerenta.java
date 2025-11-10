package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "detallerenta")
public class Detallerenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalle", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idrenta", nullable = false)
    private Renta idrenta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idarticulo", nullable = false)
    private Articulo idarticulo;

    @NotNull
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Renta getIdrenta() {
        return idrenta;
    }

    public void setIdrenta(Renta idrenta) {
        this.idrenta = idrenta;
    }

    public Articulo getIdarticulo() {
        return idarticulo;
    }

    public void setIdarticulo(Articulo idarticulo) {
        this.idarticulo = idarticulo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

}