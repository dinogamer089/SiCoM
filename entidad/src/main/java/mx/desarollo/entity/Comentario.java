package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "comentarios")
public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idComentario", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "tipo", nullable = false, columnDefinition = "ENUM('Entrega', 'Recoleccion')")
    private String tipo;

    @Lob
    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idRenta", nullable = false)
    private Renta idRenta;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Renta getIdRenta() {
        return idRenta;
    }

    public void setIdRenta(Renta idRenta) {
        this.idRenta = idRenta;
    }

}