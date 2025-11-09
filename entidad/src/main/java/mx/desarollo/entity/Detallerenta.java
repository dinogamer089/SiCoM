package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "detallerenta")
public class Detallerenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDetalle", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "cantidadRentada", nullable = false)
    private Integer cantidadRentada;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "idArticulo", nullable = false)
    private Articulo idArticulo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idRenta", nullable = false)
    private Renta idRenta;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCantidadRentada() {
        return cantidadRentada;
    }

    public void setCantidadRentada(Integer cantidadRentada) {
        this.cantidadRentada = cantidadRentada;
    }

    public Articulo getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Articulo idArticulo) {
        this.idArticulo = idArticulo;
    }

    public Renta getIdRenta() {
        return idRenta;
    }

    public void setIdRenta(Renta idRenta) {
        this.idRenta = idRenta;
    }

}