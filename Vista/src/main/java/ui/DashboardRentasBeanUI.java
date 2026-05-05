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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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

        totalCanceladas  = diarios.stream().mapToLong(DailyRentaStatusDTO::getCanceladas).sum();
        totalCompletadas = diarios.stream().mapToLong(DailyRentaStatusDTO::getCompletadas).sum();
        long total = diarios.stream().mapToLong(DailyRentaStatusDTO::getTotal).sum();
        tasaCancelacion  = total == 0 ? Double.NaN : (double) totalCanceladas / total * 100.0;

        Map<LocalDate, DailyRentaStatusDTO> porFecha = diarios.stream()
                .collect(Collectors.toMap(DailyRentaStatusDTO::getFecha, d -> d));

        buildHeatmap(porFecha);
        buildStackedBar(diarios);
    }

    private void buildHeatmap(Map<LocalDate, DailyRentaStatusDTO> porFecha) {
        // Expandir el rango al lunes anterior y domingo posterior
        LocalDate primerLunes   = fechaInicio.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate ultimoDomingo = fechaFin.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<WeekRow> rows = new ArrayList<>();
        LocalDate weekStart = primerLunes;
        int semanaNum = 1;
        int mesActual = -1;

        while (!weekStart.isAfter(ultimoDomingo)) {
            List<HeatCell> cells = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                LocalDate dia = weekStart.plusDays(i);
                boolean dentroDelRango = !dia.isBefore(fechaInicio) && !dia.isAfter(fechaFin);
                long count = 0;
                if (dentroDelRango) {
                    DailyRentaStatusDTO dto = porFecha.get(dia);
                    count = dto != null ? dto.getTotal() : 0;
                }
                cells.add(new HeatCell(dia, count, dentroDelRango));
            }

            // Detectar cambio de mes usando el miércoles de la semana como referente
            LocalDate referente = weekStart.plusDays(3);
            int mesReferente = referente.getMonthValue();
            String mesHeader = null;
            if (mesReferente != mesActual) {
                mesActual = mesReferente;
                mesHeader = referente.getMonth()
                        .getDisplayName(TextStyle.FULL, new Locale("es", "MX"))
                        .toUpperCase()
                        + " " + referente.getYear();
            }

            rows.add(new WeekRow("S" + semanaNum, cells, mesHeader));
            semanaNum++;
            weekStart = weekStart.plusWeeks(1);
        }

        // Calcular max global solo sobre días dentro del rango
        long max = rows.stream()
                .flatMap(r -> r.getCells().stream())
                .filter(HeatCell::isDentroDelRango)
                .mapToLong(c -> c.count)
                .max().orElse(0);

        // Asignar colores
        for (WeekRow row : rows) {
            for (HeatCell cell : row.getCells()) {
                cell.color = cell.isDentroDelRango()
                        ? resolveColor(cell.count, max)
                        : "#eeeeee";
            }
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
        List<Object> canceladasVals  = new ArrayList<>();
        List<String> labels          = new ArrayList<>();

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
        if (count <= 0) return "#f0f4ef";
        return "rgb(46,125,50)";
    }

    private java.util.Date toDate(LocalDate ld) {
        return java.util.Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public List<WeekRow>   getHeatmap()           { return heatmap; }
    public BarChartModel   getStackedBarModel()    { return stackedBarModel; }
    public LocalDate       getFechaInicio()        { return fechaInicio; }
    public void            setFechaInicio(LocalDate v) { this.fechaInicio = v; }
    public LocalDate       getFechaFin()           { return fechaFin; }
    public void            setFechaFin(LocalDate v)    { this.fechaFin = v; }
    public long            getTotalCanceladas()    { return totalCanceladas; }
    public long            getTotalCompletadas()   { return totalCompletadas; }
    public double          getTasaCancelacion()    { return tasaCancelacion; }
    public String          getTasaCancelacionStr() {
        if (Double.isNaN(tasaCancelacion)) return "—";
        return String.format("%.1f%%", tasaCancelacion);
    }

    // ── Clases internas ──────────────────────────────────────────────────────
    public static class WeekRow {
        private final String        label;
        private final List<HeatCell> cells;
        private final String        mesHeader; // null si no hay cambio de mes

        public WeekRow(String label, List<HeatCell> cells, String mesHeader) {
            this.label     = label;
            this.cells     = cells;
            this.mesHeader = mesHeader;
        }

        public String         getLabel()     { return label; }
        public List<HeatCell> getCells()     { return cells; }
        public String         getMesHeader() { return mesHeader; }
    }

    public static class HeatCell {
        private final LocalDate date;
        private final long      count;
        private final boolean   dentroDelRango;
        private       String    color;

        public HeatCell(LocalDate date, long count, boolean dentroDelRango) {
            this.date           = date;
            this.count          = count;
            this.dentroDelRango = dentroDelRango;
        }

        public String  getDiaLabel()      { return String.valueOf(date.getDayOfMonth()); }
        public String  getColor()         { return color; }
        public long    getCount()         { return count; }
        public boolean isDentroDelRango() { return dentroDelRango; }

        /** Texto para el tooltip: "Sábado 15 · 3 renta(s)" */
        public String getTooltip() {
            String dia = date.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, new Locale("es", "MX"));
            dia = dia.substring(0, 1).toUpperCase() + dia.substring(1);
            return dia + " " + date.getDayOfMonth() + " · " + count + " renta(s)";
        }
    }
}