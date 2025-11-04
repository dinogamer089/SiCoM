package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "articulo")
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idarticulo", nullable = false)
    private Integer idarticulo;

    @Size(max = 45)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Size(max = 10)
    @NotNull
    @Column(name = "precio", nullable = false, length = 10)
    private Integer precio;

    @Size(max = 10)
    @NotNull
    @Column(name = "cantidad", nullable = false, length = 10)
    private Integer cantidad;

    @Size(max = 45)
    @NotNull
    @Column(name = "tipo", nullable = false, length = 45)
    private String tipo;

    @Lob
    @Column(name = "imagen", columnDefinition = "LONGBLOB")
    private byte[] imagen;

    public Integer getIdarticulo(){
        return idarticulo;
    }

    public void setIdarticulo(Integer idarticulo){
        this.idarticulo = idarticulo;
    }

    public String getNombre(){
        return nombre;
    }

    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    public Integer getPrecio(){
        return precio;
    }

    public void setPrecio(Integer precio){
        this.precio = precio;
    }

    public Integer getCantidad(){
        return cantidad;
    }

    public void setCantidad(Integer cantidad){
        this.cantidad = cantidad;
    }

    public String getTipo(){
        return tipo;
    }

    public void setTipo(String tipo){
        this.tipo = tipo;
    }

    public byte[] getImagen(){
        return imagen;
    }

    public void setImagen(byte[] imagen){
        this.imagen = imagen;
    }
}