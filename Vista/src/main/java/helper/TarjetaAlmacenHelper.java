package helper;

import helper.dto.TarjetaAlmacenDTO;
import mx.desarollo.entity.MovimientoAlmacen;
import mx.desarollo.entity.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper para transformar movimientos de almacen a DTOs de tarjeta.
 * Calcula los saldos acumulados y valores contables.
 */
public class TarjetaAlmacenHelper {


    public static List<TarjetaAlmacenDTO> generarTarjeta(List<MovimientoAlmacen> movimientos, int inventarioInicial) {
        List<TarjetaAlmacenDTO> tarjeta = new ArrayList<>();

        if (movimientos == null || movimientos.isEmpty()) {
            return tarjeta;
        }

        int existenciaAcumulada = inventarioInicial;
        BigDecimal saldoAcumulado = BigDecimal.ZERO;

        // Calcular saldo inicial
        if (inventarioInicial > 0 && !movimientos.isEmpty()) {
            BigDecimal precioRef = movimientos.get(0).getPrecioUnitario();
            saldoAcumulado = precioRef.multiply(BigDecimal.valueOf(inventarioInicial));
        }

        for (MovimientoAlmacen mov : movimientos) {
            TarjetaAlmacenDTO dto = new TarjetaAlmacenDTO();

            dto.setIdMovimiento(mov.getId());
            dto.setIdRenta(mov.getRenta() != null ? mov.getRenta().getId() : null);
            dto.setFecha(mov.getFecha());
            dto.setConcepto(generarConcepto(mov));
            dto.setInventarioInicial(inventarioInicial);
            dto.setPrecioUnitario(mov.getPrecioUnitario());

            // Calcular entrada/salida segun tipo
            int entrada = 0;
            int salida = 0;
            BigDecimal debe = BigDecimal.ZERO;
            BigDecimal haber = BigDecimal.ZERO;

            if (mov.getTipoMovimiento() == TipoMovimiento.ENTRADA) {
                entrada = mov.getCantidad();
                debe = mov.getPrecioUnitario().multiply(BigDecimal.valueOf(entrada));
                existenciaAcumulada += entrada;
                saldoAcumulado = saldoAcumulado.add(debe);
            } else if (mov.getTipoMovimiento() == TipoMovimiento.SALIDA) {
                salida = mov.getCantidad();
                haber = mov.getPrecioUnitario().multiply(BigDecimal.valueOf(salida));
                existenciaAcumulada -= salida;
                saldoAcumulado = saldoAcumulado.subtract(haber);
            }

            dto.setEntrada(entrada);
            dto.setSalida(salida);
            dto.setExistencia(existenciaAcumulada);
            dto.setDebe(debe);
            dto.setHaber(haber);
            dto.setSaldo(saldoAcumulado);

            tarjeta.add(dto);
        }

        return tarjeta;
    }


    private static String generarConcepto(MovimientoAlmacen mov) {
        if (mov.getConcepto() != null && !mov.getConcepto().isBlank()) {
            return mov.getConcepto();
        }

        StringBuilder concepto = new StringBuilder();
        if (mov.getTipoMovimiento() == TipoMovimiento.ENTRADA) {
            concepto.append("Entrada");
        } else {
            concepto.append("Salida");
        }

        if (mov.getRenta() != null) {
            concepto.append(" - Renta #").append(mov.getRenta().getId());
        }

        return concepto.toString();
    }


    public static TarjetaAlmacenDTO generarFilaInicial(LocalDate fecha, int inventarioInicial, BigDecimal precioUnitario) {
        TarjetaAlmacenDTO dto = new TarjetaAlmacenDTO();
        dto.setFecha(fecha);
        dto.setConcepto("Inventario Inicial");
        dto.setInventarioInicial(inventarioInicial);
        dto.setEntrada(0);
        dto.setSalida(0);
        dto.setExistencia(inventarioInicial);
        dto.setDebe(BigDecimal.ZERO);
        dto.setHaber(BigDecimal.ZERO);

        BigDecimal saldoInicial = precioUnitario.multiply(BigDecimal.valueOf(inventarioInicial));
        dto.setSaldo(saldoInicial);
        dto.setPrecioUnitario(precioUnitario);

        return dto;
    }
}
