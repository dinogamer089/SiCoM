package mx.avanti.desarollo.dashboard;

import java.time.LocalDate;

/**
 * Punto de crecimiento acumulado de clientes.
 */
public class ClienteGrowthPoint {
    private LocalDate fecha;
    private long acumulado;

    public ClienteGrowthPoint() {}

    public ClienteGrowthPoint(LocalDate fecha, long acumulado) {
        this.fecha = fecha;
        this.acumulado = acumulado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public long getAcumulado() {
        return acumulado;
    }

    public void setAcumulado(long acumulado) {
        this.acumulado = acumulado;
    }
}
