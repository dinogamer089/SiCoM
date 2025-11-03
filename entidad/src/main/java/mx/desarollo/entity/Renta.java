package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "renta")
public class Renta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRenta", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "fecha", nullable = false, length = 45)
    private String fecha;

    @NotNull
    @Column(name = "hora", nullable = false)
    private Integer hora;

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

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Integer getHora() {
        return hora;
    }

    public void setHora(Integer hora) {
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

    // Actualiza también los Getters y Setters
    public List<Detallerenta> getDetallesRenta() {
        return detallesRenta;
    }

    public void setDetallesRenta(List<Detallerenta> detallesRenta) {
        this.detallesRenta = detallesRenta;
    }
}