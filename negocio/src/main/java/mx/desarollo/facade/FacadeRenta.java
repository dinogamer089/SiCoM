package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateRenta;
import mx.desarollo.entity.Cliente;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class FacadeRenta {

    private final DelegateRenta delegateRenta;

    public FacadeRenta() {
        this.delegateRenta = new DelegateRenta();
    }

    public Renta registrarRenta(
            Cliente cliente,
            List<Detallerenta> detalles,
            LocalDate fecha,
            LocalTime hora,
            String estado
    ) {
        return delegateRenta.registrarRentaCompleta(cliente, detalles, fecha, hora, estado);
    }
}
