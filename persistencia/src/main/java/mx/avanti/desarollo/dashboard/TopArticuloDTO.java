package mx.avanti.desarollo.dashboard;

public class TopArticuloDTO {
    private int id;
    private String nombre;
    private long cantidad;
    private double porcentaje;

    public TopArticuloDTO(int id, String nombre, long cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public long getCantidad() { return cantidad; }
    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }
}

