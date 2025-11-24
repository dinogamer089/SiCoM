package mx.avanti.desarollo.dashboard;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class DashboardMetricsDTO {
    // KPIs Actuales
    private long totalRentas;
    private BigDecimal dineroRentas;
    private BigDecimal promedioGanancia;

    // Comparativas (%)
    private double totalRentasPct;
    private double dineroRentasPct;
    private double promedioGananciaPct;

    // Valores del periodo anterior para tooltips
    private BigDecimal dineroRentasAnterior = BigDecimal.ZERO;
    private BigDecimal promedioGananciaAnterior = BigDecimal.ZERO;

    // Datos para Gráfica de Barras (Actual vs Anterior)
    private long rentasExitosasActual;
    private long rentasExitosasAnterior;

    private long rentasCanceladasActual;
    private long rentasCanceladasAnterior;

    private long cotizacionesActual;
    private long cotizacionesAnterior;

    // Datos para Gráfica de Pastel
    private List<TopArticuloDTO> topArticulos;
    private long topArticulosTotalCantidad;

    // Rangos de fecha (para mostrar en UI)
    private Date periodoActualInicio;
    private Date periodoActualFin;
    private Date periodoAnteriorInicio;
    private Date periodoAnteriorFin;

    public DashboardMetricsDTO() {
        this.dineroRentas = BigDecimal.ZERO;
        this.promedioGanancia = BigDecimal.ZERO;
    }

    public long getTotalRentas() { return totalRentas; }
    public void setTotalRentas(long totalRentas) { this.totalRentas = totalRentas; }
    public BigDecimal getDineroRentas() { return dineroRentas; }
    public void setDineroRentas(BigDecimal dineroRentas) { this.dineroRentas = dineroRentas; }
    public BigDecimal getPromedioGanancia() { return promedioGanancia; }
    public void setPromedioGanancia(BigDecimal promedioGanancia) { this.promedioGanancia = promedioGanancia; }
    public BigDecimal getDineroRentasAnterior() { return dineroRentasAnterior; }
    public void setDineroRentasAnterior(BigDecimal dineroRentasAnterior) { this.dineroRentasAnterior = dineroRentasAnterior; }
    public BigDecimal getPromedioGananciaAnterior() { return promedioGananciaAnterior; }
    public void setPromedioGananciaAnterior(BigDecimal promedioGananciaAnterior) { this.promedioGananciaAnterior = promedioGananciaAnterior; }
    public double getTotalRentasPct() { return totalRentasPct; }
    public void setTotalRentasPct(double totalRentasPct) { this.totalRentasPct = totalRentasPct; }
    public double getDineroRentasPct() { return dineroRentasPct; }
    public void setDineroRentasPct(double dineroRentasPct) { this.dineroRentasPct = dineroRentasPct; }
    public double getPromedioGananciaPct() { return promedioGananciaPct; }
    public void setPromedioGananciaPct(double promedioGananciaPct) { this.promedioGananciaPct = promedioGananciaPct; }

    public long getRentasExitosasActual() { return rentasExitosasActual; }
    public void setRentasExitosasActual(long val) { this.rentasExitosasActual = val; }
    public long getRentasExitosasAnterior() { return rentasExitosasAnterior; }
    public void setRentasExitosasAnterior(long val) { this.rentasExitosasAnterior = val; }

    public long getRentasCanceladasActual() { return rentasCanceladasActual; }
    public void setRentasCanceladasActual(long val) { this.rentasCanceladasActual = val; }
    public long getRentasCanceladasAnterior() { return rentasCanceladasAnterior; }
    public void setRentasCanceladasAnterior(long val) { this.rentasCanceladasAnterior = val; }

    public long getCotizacionesActual() { return cotizacionesActual; }
    public void setCotizacionesActual(long val) { this.cotizacionesActual = val; }
    public long getCotizacionesAnterior() { return cotizacionesAnterior; }
    public void setCotizacionesAnterior(long val) { this.cotizacionesAnterior = val; }

    public List<TopArticuloDTO> getTopArticulos() { return topArticulos; }
    public void setTopArticulos(List<TopArticuloDTO> topArticulos) { this.topArticulos = topArticulos; }
    public long getTopArticulosTotalCantidad() { return topArticulosTotalCantidad; }
    public void setTopArticulosTotalCantidad(long val) { this.topArticulosTotalCantidad = val; }

    public Date getPeriodoActualInicio() { return periodoActualInicio; }
    public void setPeriodoActualInicio(Date periodoActualInicio) { this.periodoActualInicio = periodoActualInicio; }
    public Date getPeriodoActualFin() { return periodoActualFin; }
    public void setPeriodoActualFin(Date periodoActualFin) { this.periodoActualFin = periodoActualFin; }
    public Date getPeriodoAnteriorInicio() { return periodoAnteriorInicio; }
    public void setPeriodoAnteriorInicio(Date periodoAnteriorInicio) { this.periodoAnteriorInicio = periodoAnteriorInicio; }
    public Date getPeriodoAnteriorFin() { return periodoAnteriorFin; }
    public void setPeriodoAnteriorFin(Date periodoAnteriorFin) { this.periodoAnteriorFin = periodoAnteriorFin; }
}
