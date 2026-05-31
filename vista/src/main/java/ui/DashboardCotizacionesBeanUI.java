package ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.avanti.desarollo.dashboard.CotizacionEfectividadDTO;
import mx.avanti.desarollo.dashboard.CotizacionFunnelDTO;
import mx.avanti.desarollo.dashboard.CotizacionMontosDTO;
import mx.desarollo.facade.FacadeDashboard;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("dashboardCotizacionesBean")
@ViewScoped
public class DashboardCotizacionesBeanUI implements Serializable {

    private FacadeDashboard facade;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private long cotizacionesCreadas;
    private long cotizacionesAprobadas;
    private long cotizacionesPagadas;
    private double tasaConversion;

    private BigDecimal montoCotizado = BigDecimal.ZERO;
    private BigDecimal montoCerrado = BigDecimal.ZERO;

    private List<FunnelStage> funnelStages;
    private BarChartModel efectividadModel;

    @PostConstruct
    public void init() {
        facade = new FacadeDashboard();
        LocalDate now = LocalDate.now();
        fechaInicio = now.withDayOfMonth(1);
        fechaFin = now.withDayOfMonth(now.lengthOfMonth());
        recargar();
    }

    public void recargar() {
        CotizacionFunnelDTO funnel = facade.obtenerFunnelCotizaciones(toDate(fechaInicio), toDate(fechaFin));
        CotizacionMontosDTO montos = facade.obtenerMontosCotizaciones(toDate(fechaInicio), toDate(fechaFin));
        List<CotizacionEfectividadDTO> buckets = facade.obtenerEfectividadCotizaciones(toDate(fechaInicio), toDate(fechaFin));

        cotizacionesCreadas = funnel != null ? funnel.getCreadas() : 0;
        cotizacionesAprobadas = funnel != null ? funnel.getAprobadas() : 0;
        cotizacionesPagadas = funnel != null ? funnel.getPagadas() : 0;
        tasaConversion = cotizacionesCreadas == 0 ? Double.NaN : (double) cotizacionesPagadas / (double) cotizacionesCreadas * 100.0;

        if (montos != null) {
            if (montos.getMontoCotizado() != null) {
                montoCotizado = montos.getMontoCotizado();
            }
            if (montos.getMontoCerrado() != null) {
                montoCerrado = montos.getMontoCerrado();
            }
        }

        buildFunnelStages(funnel);
        buildEfectividadChart(buckets);
    }

    private void buildFunnelStages(CotizacionFunnelDTO funnel) {
        funnelStages = new ArrayList<>();
        long base = funnel != null ? funnel.getCreadas() : 0;

        funnelStages.add(new FunnelStage(
                "Creadas",
                funnel != null ? funnel.getCreadas() : 0,
                calcPct(funnel != null ? funnel.getCreadas() : 0, base),
                "#3D4435"
        ));
        funnelStages.add(new FunnelStage(
                "Aprobadas",
                funnel != null ? funnel.getAprobadas() : 0,
                calcPct(funnel != null ? funnel.getAprobadas() : 0, base),
                "#6B7A52"
        ));
        funnelStages.add(new FunnelStage(
                "Pagadas",
                funnel != null ? funnel.getPagadas() : 0,
                calcPct(funnel != null ? funnel.getPagadas() : 0, base),
                "#90A36A"
        ));
    }

    private void buildEfectividadChart(List<CotizacionEfectividadDTO> buckets) {
        Map<String, CotizacionEfectividadDTO> porBucket = new HashMap<>();
        if (buckets != null) {
            for (CotizacionEfectividadDTO dto : buckets) {
                porBucket.put(dto.getBucket(), dto);
            }
        }

        efectividadModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet cotizadasSet = new BarChartDataSet();
        cotizadasSet.setLabel("Cotizadas");
        cotizadasSet.setBackgroundColor("#4a4a4a");
        cotizadasSet.setBorderColor("#4a4a4a");

        BarChartDataSet rentadasSet = new BarChartDataSet();
        rentadasSet.setLabel("Rentadas");
        rentadasSet.setBackgroundColor("#556b2f");
        rentadasSet.setBorderColor("#556b2f");

        String[] order = {"SMALL", "MEDIUM", "VIP"};
        String[] labels = {"Pequeno (<$5k)", "Mediano ($5k-20k)", "VIP/Boda (+$20k)"};

        List<Object> cotizadasVals = new ArrayList<>();
        List<Object> rentadasVals = new ArrayList<>();
        List<String> labelList = new ArrayList<>();

        for (int i = 0; i < order.length; i++) {
            CotizacionEfectividadDTO dto = porBucket.get(order[i]);
            long cot = dto != null ? dto.getCotizadas() : 0;
            long rent = dto != null ? dto.getRentadas() : 0;
            cotizadasVals.add(cot);
            rentadasVals.add(rent);
            labelList.add(labels[i]);
        }

        cotizadasSet.setData(cotizadasVals);
        rentadasSet.setData(rentadasVals);
        data.addChartDataSet(cotizadasSet);
        data.addChartDataSet(rentadasSet);
        data.setLabels(labelList);

        efectividadModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes yAxis = new CartesianLinearAxes();
        yAxis.setBeginAtZero(true);
        scales.addYAxesData(yAxis);
        options.setScales(scales);

        efectividadModel.setOptions(options);
    }

    private double calcPct(long value, long base) {
        if (base <= 0) {
            return Double.NaN;
        }
        return (double) value / (double) base * 100.0;
    }

    private java.util.Date toDate(LocalDate ld) {
        if (ld == null) {
            return new java.util.Date();
        }
        return java.util.Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public String formatPct(double pct) {
        if (Double.isNaN(pct)) {
            return "--";
        }
        return String.format("%.0f%%", pct);
    }

    public long getCotizacionesCreadas() {
        return cotizacionesCreadas;
    }

    public long getCotizacionesAprobadas() {
        return cotizacionesAprobadas;
    }

    public long getCotizacionesPagadas() {
        return cotizacionesPagadas;
    }

    public double getTasaConversion() {
        return tasaConversion;
    }

    public String getTasaConversionStr() {
        if (Double.isNaN(tasaConversion)) {
            return "--";
        }
        return String.format("%.1f%%", tasaConversion);
    }

    public BigDecimal getMontoCotizado() {
        return montoCotizado;
    }

    public BigDecimal getMontoCerrado() {
        return montoCerrado;
    }

    public List<FunnelStage> getFunnelStages() {
        return funnelStages;
    }

    public BarChartModel getEfectividadModel() {
        return efectividadModel;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public static class FunnelStage {
        private final String label;
        private final long value;
        private final double pct;
        private final String color;

        public FunnelStage(String label, long value, double pct, String color) {
            this.label = label;
            this.value = value;
            this.pct = pct;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public long getValue() {
            return value;
        }

        public double getPct() {
            return pct;
        }

        public String getPercentText() {
            if (Double.isNaN(pct)) return "--";
            return String.format("%.0f%%", pct);
        }

        public String getColor() {
            return color;
        }

        public int getWidthPct() {
            if (Double.isNaN(pct)) {
                return 30;
            }
            int width = (int) Math.round(pct);
            return Math.max(width, 15);
        }
    }
}
