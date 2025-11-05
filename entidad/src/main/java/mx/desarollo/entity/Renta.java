package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "renta")
public class Renta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRenta", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Size(max = 45)
    @NotNull
    @Column(name = "estado", nullable = false, length = 45)
    private String estado;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idCliente", nullable = false)
    private Cliente idCliente;

    @OneToMany(mappedBy = "idRenta")
    @OrderBy("id ASC")
    private List<Detallerenta> detallesRenta;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getFecha() { return fecha; }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() { return hora; }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Cliente getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Cliente idCliente) {
        this.idCliente = idCliente;
    }

    public List<Detallerenta> getDetallesRenta() {
        return detallesRenta;
    }

    public void setDetallesRenta(List<Detallerenta> detallesRenta) {
        this.detallesRenta = detallesRenta;
    }
}