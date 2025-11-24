package mx.avanti.desarollo.dashboard;

import java.time.LocalDate;

/**
 * Agregado diario de rentas por estado.
 */
public class DailyRentaStatusDTO {
    private LocalDate fecha;
    private long total;
    private long completadas;
    private long canceladas;

    public DailyRentaStatusDTO(LocalDate fecha, long total, long completadas, long canceladas) {
        this.fecha = fecha;
        this.total = total;
        this.completadas = completadas;
        this.canceladas = canceladas;
    }

    public LocalDate getFecha() { return fecha; }
    public long getTotal() { return total; }
    public long getCompletadas() { return completadas; }
    public long getCanceladas() { return canceladas; }
}
