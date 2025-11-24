package mx.desarollo.delegate;

import mx.avanti.desarollo.dao.DashboardDAO;
import mx.avanti.desarollo.dashboard.ClienteFrecuenciaDTO;
import mx.avanti.desarollo.dashboard.ClienteGrowthPoint;
import mx.avanti.desarollo.dashboard.CotizacionEfectividadDTO;
import mx.avanti.desarollo.dashboard.CotizacionFunnelDTO;
import mx.avanti.desarollo.dashboard.CotizacionMontosDTO;
import mx.avanti.desarollo.dashboard.DashboardMetricsDTO;
import mx.avanti.desarollo.dashboard.TopArticuloDTO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class DelegateDashboard {

    private final DashboardDAO dao;

    public DelegateDashboard() {
        this.dao = new DashboardDAO();
    }

    public long countRentasExitosas(Date start, Date end) {
        return dao.countRentasExitosas(start, end);
    }

    public long countRentasCanceladas(Date start, Date end) {
        return dao.countRentasCanceladas(start, end);
    }

    public long countCotizaciones(Date start, Date end) {
        return dao.countCotizaciones(start, end);
    }

    public BigDecimal sumTotalRentasExitosas(Date start, Date end) {
        return dao.sumTotalRentasExitosas(start, end);
    }

    public BigDecimal avgGananciaPorRenta(Date start, Date end) {
        return dao.avgGananciaPorRenta(start, end);
    }

    public List<TopArticuloDTO> findTopArticulosVendidos(Date start, Date end, int topN) {
        return dao.findTopArticulosVendidos(start, end, topN);
    }

    public long sumCantidadTotalArticulosVendidos(Date start, Date end) {
        return dao.sumCantidadTotalArticulosVendidos(start, end);
    }

    public java.util.List<mx.avanti.desarollo.dashboard.DailyRentaStatusDTO> findDailyStatus(Date start, Date end) {
        return dao.findDailyStatus(start, end);
    }

    public CotizacionFunnelDTO findCotizacionFunnel(Date start, Date end) {
        return dao.findCotizacionFunnel(start, end);
    }

    public List<CotizacionEfectividadDTO> findCotizacionesPorTamano(Date start, Date end) {
        return dao.findCotizacionesPorTamano(start, end);
    }

    public CotizacionMontosDTO sumMontosCotizaciones(Date start, Date end) {
        return dao.sumMontosCotizaciones(start, end);
    }

    public long countClientesTotales(Date start, Date end) { return dao.countClientesTotales(start, end); }
    public long countClientesNuevos(Date start, Date end) { return dao.countClientesNuevos(start, end); }
    public long countClientesFrecuentes(Date start, Date end) { return dao.countClientesFrecuentes(start, end); }
    public java.util.List<ClienteGrowthPoint> findGrowthClientesMensual(java.time.LocalDate inicio, java.time.LocalDate fin) {
        return dao.findGrowthClientesMensual(inicio, fin);
    }
    public java.util.List<ClienteFrecuenciaDTO> findClientesFrecuentes(int topN) {
        return dao.findClientesFrecuentes(topN);
    }
}
