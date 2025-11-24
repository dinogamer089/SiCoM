package mx.avanti.desarollo.dashboard;

/**
 * Agrupacion de cotizaciones vs rentas por rango de monto.
 */
public class CotizacionEfectividadDTO {

    private String bucket;
    private long cotizadas;
    private long rentadas;

    public CotizacionEfectividadDTO() {
    }

    public CotizacionEfectividadDTO(String bucket, long cotizadas, long rentadas) {
        this.bucket = bucket;
        this.cotizadas = cotizadas;
        this.rentadas = rentadas;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public long getCotizadas() {
        return cotizadas;
    }

    public void setCotizadas(long cotizadas) {
        this.cotizadas = cotizadas;
    }

    public long getRentadas() {
        return rentadas;
    }

    public void setRentadas(long rentadas) {
        this.rentadas = rentadas;
    }
}
