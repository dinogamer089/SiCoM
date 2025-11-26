package mx.avanti.desarollo.dashboard;

import java.math.BigDecimal;

/**
 * Agrupa los montos cotizados frente a los cerrados (pagados).
 */
public class CotizacionMontosDTO {

    private BigDecimal montoCotizado = BigDecimal.ZERO;
    private BigDecimal montoCerrado = BigDecimal.ZERO;

    public BigDecimal getMontoCotizado() {
        return montoCotizado;
    }

    public void setMontoCotizado(BigDecimal montoCotizado) {
        this.montoCotizado = montoCotizado;
    }

    public BigDecimal getMontoCerrado() {
        return montoCerrado;
    }

    public void setMontoCerrado(BigDecimal montoCerrado) {
        this.montoCerrado = montoCerrado;
    }
}
