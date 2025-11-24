package ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mx.avanti.desarollo.dashboard.ClienteFrecuenciaDTO;
import mx.avanti.desarollo.dashboard.ClienteGrowthPoint;
import mx.desarollo.facade.FacadeDashboard;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named("dashboardClientesBean")
@ViewScoped
public class DashboardClientesBeanUI implements Serializable {

    private FacadeDashboard facade;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private long clientesTotales;
    private long clientesNuevos;
    private long clientesFrecuentes;

    private LineChartModel growthModel;
    private BarChartModel rankingModel;

    @PostConstruct
    public void init() {
        facade = new FacadeDashboard();
        LocalDate now = LocalDate.now();
        fechaInicio = now.withDayOfMonth(1);
        fechaFin = now.withDayOfMonth(now.lengthOfMonth());
        recargar();
    }

    public void recargar() {
        clientesTotales = facade.obtenerClientesTotales(toDate(fechaInicio), toDate(fechaFin));
        clientesNuevos = facade.obtenerClientesNuevos(toDate(fechaInicio), toDate(fechaFin));
        clientesFrecuentes = facade.obtenerClientesFrecuentesCount(toDate(fechaInicio), toDate(fechaFin));

        List<ClienteGrowthPoint> growth = facade.obtenerGrowthClientes(fechaInicio, fechaFin);
        List<ClienteFrecuenciaDTO> top = facade.obtenerClientesFrecuentes(10);

        buildGrowthModel(growth);
        buildRankingModel(top);
    }

    private void buildGrowthModel(List<ClienteGrowthPoint> growth) {
        growthModel = new LineChartModel();
        ChartData data = new ChartData();

        LineChartDataSet dataSet = new LineChartDataSet();
        dataSet.setLabel("Clientes acumulados");

        List<Object> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Ordenar por fecha por seguridad
        if (growth != null) {
            growth.sort(Comparator.comparing(ClienteGrowthPoint::getFecha));
            for (ClienteGrowthPoint p : growth) {
                labels.add(String.valueOf(p.getFecha().getDayOfMonth()));
                values.add(p.getAcumulado());
            }
        }

        dataSet.setData(values);
        dataSet.setFill(true);
        dataSet.setBorderColor("#3D4435");
        dataSet.setBackgroundColor("rgba(61,68,53,0.25)");
        dataSet.setTension(0.3);

        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        growthModel.setData(data);

        LineChartOptions options = new LineChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes yAxis = new CartesianLinearAxes();
        yAxis.setBeginAtZero(true);
        scales.addYAxesData(yAxis);
        options.setScales(scales);
        growthModel.setOptions(options);
    }

    private void buildRankingModel(List<ClienteFrecuenciaDTO> top) {
        rankingModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Interacciones");
        dataSet.setBackgroundColor("#6B7A52");
        dataSet.setBorderColor("#6B7A52");

        List<Object> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        List<ClienteFrecuenciaDTO> ordered = top != null ? new ArrayList<>(top) : Collections.emptyList();
        ordered.sort(Comparator.comparingLong(ClienteFrecuenciaDTO::getConteo).reversed());

        for (ClienteFrecuenciaDTO dto : ordered) {
            values.add(dto.getConteo());
            labels.add(dto.getNombre());
        }

        dataSet.setData(values);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        rankingModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        // Horizontal
        options.setIndexAxis("y");

        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes xAxis = new CartesianLinearAxes();
        xAxis.setBeginAtZero(true);
        scales.addXAxesData(xAxis);
        options.setScales(scales);

        rankingModel.setOptions(options);
        rankingModel.setExtender("clientesBarExt");
    }

    private java.util.Date toDate(LocalDate ld) {
        if (ld == null) return new java.util.Date();
        return java.util.Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public long getClientesTotales() {
        return clientesTotales;
    }

    public long getClientesNuevos() {
        return clientesNuevos;
    }

    public long getClientesFrecuentes() {
        return clientesFrecuentes;
    }

    public LineChartModel getGrowthModel() {
        return growthModel;
    }

    public BarChartModel getRankingModel() {
        return rankingModel;
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
}
