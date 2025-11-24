package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateDashboard;
import mx.avanti.desarollo.dashboard.ClienteFrecuenciaDTO;
import mx.avanti.desarollo.dashboard.ClienteGrowthPoint;
import mx.avanti.desarollo.dashboard.CotizacionEfectividadDTO;
import mx.avanti.desarollo.dashboard.CotizacionFunnelDTO;
import mx.avanti.desarollo.dashboard.CotizacionMontosDTO;
import mx.avanti.desarollo.dashboard.DashboardMetricsDTO;
import mx.avanti.desarollo.dashboard.TopArticuloDTO;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FacadeDashboard {

    private final DelegateDashboard delegate = new DelegateDashboard();

    public DashboardMetricsDTO calcularMetricas(String rango, int nDias) {
        Date start, end, startPrev, endPrev;
        Date now = new Date();
        Calendar cal = Calendar.getInstance();

        if ("HOY".equals(rango)) {
            start = getStartOfDay(now);
            end = getEndOfDay(now);

            cal.setTime(start); cal.add(Calendar.DAY_OF_YEAR, -1);
            startPrev = cal.getTime();
            cal.setTime(end); cal.add(Calendar.DAY_OF_YEAR, -1);
            endPrev = cal.getTime();

        } else if ("ULTIMOS_30".equals(rango)) {
            // Incluye el día actual en el rango
            end = getEndOfDay(now);
            cal.setTime(now);
            cal.add(Calendar.DAY_OF_YEAR, -(30 - 1)); // hace 29 días
            start = getStartOfDay(cal.getTime());

            // Ventana comparativa inmediatamente anterior (30 días previos al bloque actual)
            cal.setTime(start); cal.add(Calendar.DAY_OF_YEAR, -30);
            startPrev = cal.getTime();
            cal.setTime(end); cal.add(Calendar.DAY_OF_YEAR, -30);
            endPrev = cal.getTime();

        } else { // ULTIMOS_N
            // Incluye el día actual
            end = getEndOfDay(now);
            cal.setTime(now);
            cal.add(Calendar.DAY_OF_YEAR, -(nDias - 1));
            start = getStartOfDay(cal.getTime());

            // Ventana comparativa inmediatamente anterior de N días
            cal.setTime(start); cal.add(Calendar.DAY_OF_YEAR, -nDias);
            startPrev = cal.getTime();
            cal.setTime(end); cal.add(Calendar.DAY_OF_YEAR, -nDias);
            endPrev = cal.getTime();
        }

        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        // Rangos para mostrar en UI
        dto.setPeriodoActualInicio(start);
        dto.setPeriodoActualFin(end);
        dto.setPeriodoAnteriorInicio(startPrev);
        dto.setPeriodoAnteriorFin(endPrev);

        dto.setTotalRentas(delegate.countRentasExitosas(start, end));
        dto.setDineroRentas(delegate.sumTotalRentasExitosas(start, end));
        dto.setPromedioGanancia(delegate.avgGananciaPorRenta(start, end));

        dto.setRentasExitosasActual(dto.getTotalRentas());
        dto.setRentasCanceladasActual(delegate.countRentasCanceladas(start, end));
        dto.setCotizacionesActual(delegate.countCotizaciones(start, end));

        long totalPrev = delegate.countRentasExitosas(startPrev, endPrev);
        BigDecimal dineroPrev = delegate.sumTotalRentasExitosas(startPrev, endPrev);
        BigDecimal avgPrev = delegate.avgGananciaPorRenta(startPrev, endPrev);

        dto.setDineroRentasAnterior(dineroPrev);
        dto.setPromedioGananciaAnterior(avgPrev);

        dto.setRentasExitosasAnterior(totalPrev);
        dto.setRentasCanceladasAnterior(delegate.countRentasCanceladas(startPrev, endPrev));
        dto.setCotizacionesAnterior(delegate.countCotizaciones(startPrev, endPrev));

        dto.setTotalRentasPct(calculateChange(dto.getTotalRentas(), totalPrev));
        dto.setDineroRentasPct(calculateChange(dto.getDineroRentas().doubleValue(), dineroPrev.doubleValue()));
        dto.setPromedioGananciaPct(calculateChange(dto.getPromedioGanancia().doubleValue(), avgPrev.doubleValue()));

        List<TopArticuloDTO> topList = delegate.findTopArticulosVendidos(start, end, 5);
        long totalCantidadRango = delegate.sumCantidadTotalArticulosVendidos(start, end);
        dto.setTopArticulosTotalCantidad(totalCantidadRango);

        long sumTop = 0;
        for (TopArticuloDTO item : topList) {
            sumTop += item.getCantidad();
            if (totalCantidadRango > 0) {
                item.setPorcentaje((double) item.getCantidad() / totalCantidadRango * 100.0);
            }
        }
        if (totalCantidadRango > sumTop) {
            TopArticuloDTO otros = new TopArticuloDTO(0, "Otros", totalCantidadRango - sumTop);
            if (totalCantidadRango > 0) {
                otros.setPorcentaje((double) otros.getCantidad() / totalCantidadRango * 100.0);
            }
            topList.add(otros);
        }
        dto.setTopArticulos(topList);

        return dto;
    }

    private static double calculateChange(double current, double previous) {
        if (previous == 0) {
            // Devolver NaN para que la vista muestre —
            return Double.NaN;
        }
        return ((current - previous) / previous) * 100.0;
    }

    private static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public List<mx.avanti.desarollo.dashboard.DailyRentaStatusDTO> obtenerDailyStatus(Date start, Date end) {
        return delegate.findDailyStatus(start, end);
    }

    public CotizacionFunnelDTO obtenerFunnelCotizaciones(Date start, Date end) {
        return delegate.findCotizacionFunnel(start, end);
    }

    public List<CotizacionEfectividadDTO> obtenerEfectividadCotizaciones(Date start, Date end) {
        return delegate.findCotizacionesPorTamano(start, end);
    }

    public CotizacionMontosDTO obtenerMontosCotizaciones(Date start, Date end) {
        return delegate.sumMontosCotizaciones(start, end);
    }

    // Clientes
    public long obtenerClientesTotales(Date start, Date end) { return delegate.countClientesTotales(start, end); }
    public long obtenerClientesNuevos(Date start, Date end) { return delegate.countClientesNuevos(start, end); }
    public long obtenerClientesFrecuentesCount(Date start, Date end) { return delegate.countClientesFrecuentes(start, end); }
    public java.util.List<ClienteGrowthPoint> obtenerGrowthClientes(java.time.LocalDate inicio, java.time.LocalDate fin) {
        return delegate.findGrowthClientesMensual(inicio, fin);
    }
    public java.util.List<ClienteFrecuenciaDTO> obtenerClientesFrecuentes(int topN) {
        return delegate.findClientesFrecuentes(topN);
    }
}
