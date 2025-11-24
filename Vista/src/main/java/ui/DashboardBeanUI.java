package ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.avanti.desarollo.dashboard.DashboardMetricsDTO;
import mx.avanti.desarollo.dashboard.TopArticuloDTO;
import mx.desarollo.facade.FacadeDashboard;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Named("dashboardBean")
@ViewScoped
public class DashboardBeanUI implements Serializable {

    private FacadeDashboard facade;
    private DashboardMetricsDTO metrics;

    private String rangoSeleccionado = "HOY"; // HOY, ULTIMOS_30, ULTIMOS_N
    private int diasN = 7; // Default N

    private BarChartModel barModel;
    private PieChartModel pieModel;

    @PostConstruct
    public void init() {
        this.facade = new FacadeDashboard();
        updateDashboard();
    }

    public void updateDashboard() {
        this.metrics = facade.calcularMetricas(rangoSeleccionado, diasN);
        createBarModel();
        createPieModel();
    }

    @SuppressWarnings({"removal", "deprecation"})
    private void createBarModel() {
        barModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSetActual = new BarChartDataSet();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String actualLabel = String.format("Actual (%s - %s)",
                sdf.format(metrics.getPeriodoActualInicio()),
                sdf.format(metrics.getPeriodoActualFin()));
        dataSetActual.setLabel(actualLabel);
        List<Object> valuesActual = new ArrayList<>();
        valuesActual.add(metrics.getRentasExitosasActual());
        valuesActual.add(metrics.getRentasCanceladasActual());
        valuesActual.add(metrics.getCotizacionesActual());
        dataSetActual.setData(valuesActual);
        dataSetActual.setBackgroundColor("rgba(75, 192, 192, 0.2)");
        dataSetActual.setBorderColor("rgb(75, 192, 192)");
        dataSetActual.setBorderWidth(1);

        BarChartDataSet dataSetPrev = new BarChartDataSet();
        String previoLabel = String.format("Comparativo (%s - %s)",
                sdf.format(metrics.getPeriodoAnteriorInicio()),
                sdf.format(metrics.getPeriodoAnteriorFin()));
        dataSetPrev.setLabel(previoLabel);
        List<Object> valuesPrev = new ArrayList<>();
        valuesPrev.add(metrics.getRentasExitosasAnterior());
        valuesPrev.add(metrics.getRentasCanceladasAnterior());
        valuesPrev.add(metrics.getCotizacionesAnterior());
        dataSetPrev.setData(valuesPrev);
        dataSetPrev.setBackgroundColor("rgba(201, 203, 207, 0.2)");
        dataSetPrev.setBorderColor("rgb(201, 203, 207)");
        dataSetPrev.setBorderWidth(1);

        data.addChartDataSet(dataSetActual);
        data.addChartDataSet(dataSetPrev);

        List<String> labels = new ArrayList<>();
        labels.add("Exitosas");
        labels.add("Canceladas");
        labels.add("Cotizaciones");
        data.setLabels(labels);

        barModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        linearAxes.setBeginAtZero(true);
        scales.addYAxesData(linearAxes);
        options.setScales(scales);
        barModel.setOptions(options);
    }

    @SuppressWarnings({"removal", "deprecation"})
    private void createPieModel() {
        pieModel = new PieChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();

        String[] colors = {"rgb(255, 99, 132)", "rgb(54, 162, 235)", "rgb(255, 205, 86)", "rgb(75, 192, 192)", "rgb(153, 102, 255)", "rgb(201, 203, 207)"};

        int i = 0;
        if (metrics.getTopArticulos() != null) {
            for (TopArticuloDTO art : metrics.getTopArticulos()) {
                values.add(art.getCantidad());
                labels.add(art.getNombre());
                bgColors.add(colors[i % colors.length]);
                i++;
            }
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        pieModel.setData(data);
    }

    public String getPctColor(double pct) {
        if (Double.isNaN(pct)) return "gray";
        if (pct > 0) return "green";
        if (pct < 0) return "red";
        return "gray";
    }

    public String formatPct(double pct) {
        if (Double.isNaN(pct)) return "—";
        if (pct == 0) return "—";
        return String.format("%+.1f%%", pct);
    }

    public DashboardMetricsDTO getMetrics() { return metrics; }
    public String getRangoSeleccionado() { return rangoSeleccionado; }
    public void setRangoSeleccionado(String rangoSeleccionado) { this.rangoSeleccionado = rangoSeleccionado; }
    public int getDiasN() { return diasN; }
    public void setDiasN(int diasN) { this.diasN = diasN; }
    public BarChartModel getBarModel() { return barModel; }
    public PieChartModel getPieModel() { return pieModel; }

    private String buildComparativoFrase(double pct) {
        String pctTxt = formatPct(pct);
        switch (rangoSeleccionado) {
            case "HOY":
                return String.format("hoy %s vs ayer", pctTxt);
            case "ULTIMOS_30":
                return String.format("últ. 30 días %s vs previos", pctTxt);
            case "ULTIMOS_N":
                return String.format("últ. %d días %s vs previos", diasN, pctTxt);
            default:
                return String.format("%s vs anterior", pctTxt);
        }
    }

    public String getFraseTotalRentas() { return (metrics != null) ? buildComparativoFrase(metrics.getTotalRentasPct()) : buildComparativoFrase(Double.NaN); }
    public String getFraseDineroRentas() { return (metrics != null) ? buildComparativoFrase(metrics.getDineroRentasPct()) : buildComparativoFrase(Double.NaN); }
    public String getFrasePromedioGanancia() { return (metrics != null) ? buildComparativoFrase(metrics.getPromedioGananciaPct()) : buildComparativoFrase(Double.NaN); }
}

