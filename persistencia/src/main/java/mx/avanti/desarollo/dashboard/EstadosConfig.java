package mx.avanti.desarollo.dashboard;

import java.util.Set;

/**
 * Configuración de estados de negocio para KPIs del dashboard.
 * Deben usarse tal cual según la especificación del usuario.
 */
public final class EstadosConfig {
    private EstadosConfig() {}

    /** Estados que deben contarse como exitosas (todo lo que no sea solicitud o cancelada) */
    public static final Set<String> EXITO = Set.of("Finalizadas", "Finalizada", "Confirmado");
    /** Estados de cancelación explícita */
    public static final Set<String> CANCELADA = Set.of("Cancelada");
    /** Estados que representan una solicitud/cotización */
    public static final Set<String> COTIZACIONES = Set.of("SOLICITADA", "SOLICITADAS");
    public static final String PENDIENTE_PAGO = "Aprobada";
}
