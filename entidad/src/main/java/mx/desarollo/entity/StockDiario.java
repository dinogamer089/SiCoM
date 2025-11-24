package mx.desarollo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que almacena el inventario diario de cada articulo.
 * Se genera un registro por articulo por dia para llevar control del stock.
 */
@Entity
@Table(name = "stock_diario", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"idarticulo", "fecha"})
})
public class StockDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idarticulo", nullable = false)
    private Articulo articulo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** Stock disponible al inicio del dia */
    @Column(name = "inventario_inicial", nullable = false)
    private Integer inventarioInicial;

    /** Stock disponible al final del dia */
    @Column(name = "existencia_final", nullable = false)
    private Integer existenciaFinal;

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getInventarioInicial() {
        return inventarioInicial;
    }

    public void setInventarioInicial(Integer inventarioInicial) {
        this.inventarioInicial = inventarioInicial;
    }

    public Integer getExistenciaFinal() {
        return existenciaFinal;
    }

    public void setExistenciaFinal(Integer existenciaFinal) {
        this.existenciaFinal = existenciaFinal;
    }
}
