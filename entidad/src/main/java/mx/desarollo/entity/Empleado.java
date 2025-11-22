package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idempleado", nullable = false)
    private Integer id;

    @Size(max = 80)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Size(max = 45)
    @NotNull
    @Column(name = "apellido_paterno", nullable = false, length = 45)
    private String apellidoPaterno;

    @Size(max = 45)
    @NotNull
    @Column(name = "apellido_materno", nullable = false, length = 45)
    private String apellidoMaterno;

    @Size(max = 45)
    @NotNull
    @Column(name = "correo", nullable = false, length = 45, unique = true)
    private String correo;

    @Size(max = 255)
    @NotNull
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    // --- Getters y Setters ---
    // (Tus getters y setters actuales están perfectos) [cite: 7-19]
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    @Transient
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " +
                (apellidoPaterno != null ? apellidoPaterno : "") + " " +
                (apellidoMaterno != null ? apellidoMaterno : "");
    }
}