package helper.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DTO que representa una fila en la tarjeta de almacen.
 * Contiene toda la informacion contable del movimiento.
 */
public class TarjetaAlmacenDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Integer idMovimiento;
    private Integer idRenta;
    private LocalDate fecha;
    private String concepto;

    // Columnas de movimiento fisico
    private Integer inventarioInicial;
    private Integer entrada;
    private Integer salida;
    private Integer existencia;

    // Columnas contables
    private BigDecimal debe;        // entrada * precio
    private BigDecimal haber;       // salida * precio
    private BigDecimal saldo;       // saldo acumulado

    private BigDecimal precioUnitario;

    // Getters y Setters
    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Integer getIdRenta() {
        return idRenta;
    }

    public void setIdRenta(Integer idRenta) {
        this.idRenta = idRenta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Integer getInventarioInicial() {
        return inventarioInicial;
    }

    public void setInventarioInicial(Integer inventarioInicial) {
        this.inventarioInicial = inventarioInicial;
    }

    public Integer getEntrada() {
        return entrada;
    }

    public void setEntrada(Integer entrada) {
        this.entrada = entrada;
    }

    public Integer getSalida() {
        return salida;
    }

    public void setSalida(Integer salida) {
        this.salida = salida;
    }

    public Integer getExistencia() {
        return existencia;
    }

    public void setExistencia(Integer existencia) {
        this.existencia = existencia;
    }

    public BigDecimal getDebe() {
        return debe;
    }

    public void setDebe(BigDecimal debe) {
        this.debe = debe;
    }

    public BigDecimal getHaber() {
        return haber;
    }

    public void setHaber(BigDecimal haber) {
        this.haber = haber;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    /**
     * Retorna la fecha formateada como String para JSF.
     * @return Fecha en formato dd/MM/yyyy
     */
    public String getFechaFormateada() {
        return fecha != null ? fecha.format(FORMATTER) : "";
    }

    /**
     * Verifica si el debe es mayor a cero.
     * @return true si debe > 0
     */
    public boolean isDebePositivo() {
        return debe != null && debe.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Verifica si el haber es mayor a cero.
     * @return true si haber > 0
     */
    public boolean isHaberPositivo() {
        return haber != null && haber.compareTo(BigDecimal.ZERO) > 0;
    }
}
