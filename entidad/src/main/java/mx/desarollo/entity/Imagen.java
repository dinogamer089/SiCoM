package mx.desarollo.entity;

import jakarta.persistence.*;

/**
 * Imagen binaria con su MIME Relacion 1:1 desde Articulo
 */
@Entity
@Table(name = "imagen")
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long id;

    /** Contenido binario de la imagen */
    @Lob
    @Column(name = "datos", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] datos;

    /** Tipo MIME ejemplos: image/png, image/jpeg */
    @Column(name = "mime", nullable = false, length = 50)
    private String mime;

    // Getters y Setters
    public Long getId() { return id; }
    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }
    public String getMime() { return mime; }
    public void setMime(String mime) { this.mime = mime; }
}
