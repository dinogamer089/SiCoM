package mx.desarollo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "combinacion_mesa",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mesa_id", "mantel_id", "camino_id", "cubre_id"}))
public class CombinacionMesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Articulo mesa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mantel_id", nullable = false)
    private Articulo mantel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camino_id")
    private Articulo camino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cubre_id")
    private Articulo cubre;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "imagen_id", nullable = false, unique = true)
    private Imagen imagen;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public Integer getId() { return id; }
    public Articulo getMesa() { return mesa; }
    public void setMesa(Articulo mesa) { this.mesa = mesa; }
    public Articulo getMantel() { return mantel; }
    public void setMantel(Articulo mantel) { this.mantel = mantel; }
    public Articulo getCamino() { return camino; }
    public void setCamino(Articulo camino) { this.camino = camino; }
    public Articulo getCubre() { return cubre; }
    public void setCubre(Articulo cubre) { this.cubre = cubre; }
    public Imagen getImagen() { return imagen; }
    public void setImagen(Imagen imagen) { this.imagen = imagen; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}

