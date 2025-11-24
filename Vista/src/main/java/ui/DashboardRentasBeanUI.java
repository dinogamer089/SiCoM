package ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.avanti.desarollo.dashboard.DailyRentaStatusDTO;
import mx.desarollo.facade.FacadeDashboard;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Named("dashboardRentasBean")
@ViewScoped
public class DashboardRentasBeanUI implements Serializable {

    private FacadeDashboard facade;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private List<WeekRow> heatmap;
    private BarChartModel stackedBarModel;

    private long totalCanceladas;
    private long totalCompletadas;
    private double tasaCancelacion;

    @PostConstruct
    public void init() {
        facade = new FacadeDashboard();
        LocalDate now = LocalDate.now();
        fechaInicio = now.withDayOfMonth(1);
        fechaFin = now.withDayOfMonth(now.lengthOfMonth());
        recargar();
    }

    public void recargar() {
        List<DailyRentaStatusDTO> diarios = facade.obtenerDailyStatus(
                toDate(fechaInicio),
                toDate(fechaFin));
        if (diarios == null) {
            diarios = Collections.emptyList();
        }

        // Totales para KPIs
        totalCanceladas = diarios.stream().mapToLong(DailyRentaStatusDTO::getCanceladas).sum();
        totalCompletadas = diarios.stream().mapToLong(DailyRentaStatusDTO::getCompletadas).sum();
        long total = diarios.stream().mapToLong(DailyRentaStatusDTO::getTotal).sum();
        tasaCancelacion = total == 0 ? Double.NaN : (double) totalCanceladas / total * 100.0;

        // Preparar bar chart y heatmap
        Map<LocalDate, DailyRentaStatusDTO> porFecha = diarios.stream()
                .collect(Collectors.toMap(DailyRentaStatusDTO::getFecha, d -> d));

        buildHeatmap(porFecha);
        buildStackedBar(diarios);
    }

    private void buildHeatmap(Map<LocalDate, DailyRentaStatusDTO> porFecha) {
        List<HeatCell> allCells = new ArrayList<>();
        LocalDate cursor = fechaInicio;
        while (!cursor.isAfter(fechaFin)) {
            DailyRentaStatusDTO dto = porFecha.get(cursor);
            long count = dto != null ? dto.getTotal() : 0;
            allCells.add(new HeatCell(cursor, count));
            cursor = cursor.plusDays(1);
        }
        long max = allCells.stream().mapToLong(c -> c.count).max().orElse(0);
        for (HeatCell c : allCells) {
            c.color = resolveColor(c.count, max);
        }

        // Agrupar por semana del mes
        WeekFields wf = WeekFields.of(Locale.getDefault());
        Map<Integer, List<HeatCell>> porSemana = allCells.stream()
                .collect(Collectors.groupingBy(c -> c.date.get(wf.weekOfMonth())));

        List<Integer> semanas = new ArrayList<>(porSemana.keySet());
        Collections.sort(semanas);
        List<WeekRow> rows = new ArrayList<>();
        for (Integer semana : semanas) {
            List<HeatCell> cells = porSemana.get(semana);
            cells.sort(Comparator.comparing(c -> c.date.getDayOfWeek().getValue()));
            rows.add(new WeekRow("S" + semana, cells));
        }
        heatmap = rows;
    }

    private void buildStackedBar(List<DailyRentaStatusDTO> diarios) {
        diarios.sort(Comparator.comparing(DailyRentaStatusDTO::getFecha));

        stackedBarModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet completadas = new BarChartDataSet();
        completadas.setLabel("Rentas Completadas");
        completadas.setBackgroundColor("rgba(67,160,71,0.8)");
        completadas.setBorderColor("rgba(56,142,60,1)");
        completadas.setStack("rentas");

        BarChartDataSet canceladas = new BarChartDataSet();
        canceladas.setLabel("Rentas Canceladas");
        canceladas.setBackgroundColor("rgba(229,57,53,0.8)");
        canceladas.setBorderColor("rgba(198,40,40,1)");
        canceladas.setStack("rentas");

        List<Object> completadasVals = new ArrayList<>();
        List<Object> canceladasVals = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (DailyRentaStatusDTO dto : diarios) {
            labels.add(String.valueOf(dto.getFecha().getDayOfMonth()));
            completadasVals.add(dto.getCompletadas());
            canceladasVals.add(dto.getCanceladas());
        }

        completadas.setData(completadasVals);
        canceladas.setData(canceladasVals);

        data.addChartDataSet(completadas);
        data.addChartDataSet(canceladas);
        data.setLabels(labels);

        stackedBarModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes yAxis = new CartesianLinearAxes();
        yAxis.setBeginAtZero(true);
        yAxis.setStacked(true);
        scales.addYAxesData(yAxis);
        CartesianLinearAxes xAxis = new CartesianLinearAxes();
        xAxis.setStacked(true);
        scales.addXAxesData(xAxis);
        options.setScales(scales);
        stackedBarModel.setOptions(options);
    }

    private String resolveColor(long count, long max) {
        if (count <= 0 || max <= 0) {
            return "#f5f5f5";
        }
        double ratio = (double) count / (double) max;
        double intensity = 0.35 + (0.65 * ratio);
        int r = (int) Math.round(46 * intensity);
        int g = (int) Math.round(125 * intensity);
        int b = (int) Math.round(50 * intensity);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }

    private java.util.Date toDate(LocalDate ld) {
        return java.util.Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Getters y modelos para la vista
    public List<WeekRow> getHeatmap() { return heatmap; }
    public BarChartModel getStackedBarModel() { return stackedBarModel; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public long getTotalCanceladas() { return totalCanceladas; }
    public long getTotalCompletadas() { return totalCompletadas; }
    public double getTasaCancelacion() { return tasaCancelacion; }
    public String getTasaCancelacionStr() {
        if (Double.isNaN(tasaCancelacion)) return "—";
        return String.format("%.1f%%", tasaCancelacion);
    }

    // Row y Cell para heatmap
    public static class WeekRow {
        private final String label;
        private final List<HeatCell> cells;

        public WeekRow(String label, List<HeatCell> cells) {
            this.label = label;
            this.cells = cells;
        }

        public String getLabel() { return label; }
        public List<HeatCell> getCells() { return cells; }
    }

    public static class HeatCell {
        private final LocalDate date;
        private final long count;
        private String color;

        public HeatCell(LocalDate date, long count) {
            this.date = date;
            this.count = count;
        }

        public String getDiaLabel() {
            return String.valueOf(date.getDayOfMonth());
        }

        public String getColor() { return color; }
        public long getCount() { return count; }
    }
}
