package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Cliente;
import mx.desarollo.entity.Detallerenta;
import mx.desarollo.entity.Renta;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DelegateRenta {

    public Renta registrarRentaCompleta(
            Cliente cliente,
            List<Detallerenta> detalles,
            LocalDate fecha,
            LocalTime hora,
            String estado
    ) {
        return ServiceLocator.getInstanceRentaDAO()
                .registrarRentaCompleta(cliente, detalles, fecha, hora, estado);
    }
}
