package mx.avanti.desarollo.dashboard;

/**
 * Totales por etapa del ciclo de cotizaciones.
 */
public class CotizacionFunnelDTO {

    private long creadas;
    private long aprobadas;
    private long pagadas;

    public long getCreadas() {
        return creadas;
    }

    public void setCreadas(long creadas) {
        this.creadas = creadas;
    }

    public long getAprobadas() {
        return aprobadas;
    }

    public void setAprobadas(long aprobadas) {
        this.aprobadas = aprobadas;
    }

    public long getPagadas() {
        return pagadas;
    }

    public void setPagadas(long pagadas) {
        this.pagadas = pagadas;
    }
}
