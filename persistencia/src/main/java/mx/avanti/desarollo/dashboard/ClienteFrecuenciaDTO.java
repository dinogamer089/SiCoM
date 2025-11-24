package mx.avanti.desarollo.dashboard;

/**
 * Cliente y numero de interacciones (rentas/cotizaciones).
 */
public class ClienteFrecuenciaDTO {
    private String nombre;
    private long conteo;

    public ClienteFrecuenciaDTO() {}

    public ClienteFrecuenciaDTO(String nombre, long conteo) {
        this.nombre = nombre;
        this.conteo = conteo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getConteo() {
        return conteo;
    }

    public void setConteo(long conteo) {
        this.conteo = conteo;
    }
}
