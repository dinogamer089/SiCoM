package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "stock_reservado_diario")
public class StockReservadoDiario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock_reservado", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idarticulo", nullable = false)
    private Articulo idarticulo;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad_reservada", columnDefinition = "int UNSIGNED not null")
    private Long cantidadReservada;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Articulo getIdarticulo() {
        return idarticulo;
    }

    public void setIdarticulo(Articulo idarticulo) {
        this.idarticulo = idarticulo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getCantidadReservada() {
        return cantidadReservada;
    }

    public void setCantidadReservada(Long cantidadReservada) {
        this.cantidadReservada = cantidadReservada;
    }

}