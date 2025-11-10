package mx.desarollo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

// Clase entidad que mapea la tabla 'cliente' de la base de datos
@Entity
@Table(name = "cliente")
public class Cliente implements Serializable {

    // Identificador único del cliente (clave primaria autoincrementable)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCliente", nullable = false)
    private Integer idCliente;

    // Nombre del cliente, campo obligatorio con validación de tamaño
    @NotBlank
    @Size(max = 45)
    @Column(name = "nombre", length = 45, nullable = false)
    private String nombre;

    // Teléfono del cliente
    @Size(max = 45)
    @Column(name = "telefono", length = 45)
    private String telefono;

    // Dirección del cliente
    @Size(max = 45)
    @Column(name = "direccion", length = 45)
    private String direccion;

    // Métodos getters y setters estándar para acceder a los datos
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}