package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "renta")
public class Renta implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrenta", nullable = false)
    private Integer idrenta;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @NotNull
    @Digits(integer = 8, fraction = 2)
    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @NotNull
    @Size(max = 45)
    @Column(name = "estado", length = 45, nullable = false)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCliente", referencedColumnName = "idCliente", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "renta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Detallerenta> detalles = new ArrayList<>();

    // helpers
    public void addDetalle(Detallerenta d) {
        d.setRenta(this);
        detalles.add(d);
    }

    // getters/setters
    public Integer getIdrenta() { return idrenta; }
    public void setIdrenta(Integer idrenta) { this.idrenta = idrenta; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<Detallerenta> getDetalles() { return detalles; }
    public void setDetalles(List<Detallerenta> detalles) { this.detalles = detalles; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
}
