package mx.avanti.desarollo.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import mx.avanti.desarollo.dashboard.CotizacionEfectividadDTO;
import mx.avanti.desarollo.dashboard.CotizacionFunnelDTO;
import mx.avanti.desarollo.dashboard.CotizacionMontosDTO;
import mx.avanti.desarollo.dashboard.ClienteFrecuenciaDTO;
import mx.avanti.desarollo.dashboard.ClienteGrowthPoint;
import mx.avanti.desarollo.dashboard.EstadosConfig;
import mx.avanti.desarollo.dashboard.TopArticuloDTO;
import mx.avanti.desarollo.persistence.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class DashboardDAO {

    private EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // --- Conteos ---
    public long countRentasExitosas(Date start, Date end) {
        // Cuenta todas las rentas en rango de fecha excepto solicitudes y canceladas
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(r) FROM Renta r " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL) " +
                    "AND r.estado NOT IN :noEstados";
            Query q = em.createQuery(jpql);
            java.util.Set<String> noEstados = new java.util.HashSet<>();
            noEstados.addAll(EstadosConfig.CANCELADA);
            noEstados.addAll(EstadosConfig.COTIZACIONES);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            q.setParameter("noEstados", noEstados);
            return ((Number) q.getSingleResult()).longValue();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public long countRentasCanceladas(Date start, Date end) {
        return executeCount(start, end, EstadosConfig.CANCELADA);
    }

    public long countCotizaciones(Date start, Date end) {
        // Todas las rentas se consideran cotizaciones, sin importar estado ni fecha
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(r) FROM Renta r";
            Query q = em.createQuery(jpql);
            return ((Number) q.getSingleResult()).longValue();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    private long executeCount(Date start, Date end, Set<String> estados) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(r) FROM Renta r " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL) " +
                    "AND r.estado IN :estados";
            Query q = em.createQuery(jpql);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            q.setParameter("estados", estados);
            return ((Number) q.getSingleResult()).longValue();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    // --- Agregados ---
    public BigDecimal sumTotalRentasExitosas(Date start, Date end) {
        // Suma de totales excluyendo solicitudes y canceladas
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COALESCE(SUM(r.total), 0) FROM Renta r " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL) " +
                    "AND r.estado NOT IN :noEstados";
            Query q = em.createQuery(jpql);
            java.util.Set<String> noEstados = new java.util.HashSet<>();
            noEstados.addAll(EstadosConfig.CANCELADA);
            noEstados.addAll(EstadosConfig.COTIZACIONES);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            q.setParameter("noEstados", noEstados);
            BigDecimal res = (BigDecimal) q.getSingleResult();
            return (res != null) ? res : BigDecimal.ZERO;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public BigDecimal avgGananciaPorRenta(Date start, Date end) {
        // Promedio excluyendo solicitudes y canceladas
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COALESCE(AVG(r.total), 0) FROM Renta r " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL) " +
                    "AND r.estado NOT IN :noEstados";
            Query q = em.createQuery(jpql);
            java.util.Set<String> noEstados = new java.util.HashSet<>();
            noEstados.addAll(EstadosConfig.CANCELADA);
            noEstados.addAll(EstadosConfig.COTIZACIONES);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            q.setParameter("noEstados", noEstados);
            Double val = (Double) q.getSingleResult();
            return BigDecimal.valueOf(val != null ? val : 0.0);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Agregado diario: totales, completadas y canceladas dentro del rango.
     */
    public List<mx.avanti.desarollo.dashboard.DailyRentaStatusDTO> findDailyStatus(Date start, Date end) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT new mx.avanti.desarollo.dashboard.DailyRentaStatusDTO(" +
                    "r.fecha, " +
                    "COUNT(r), " +
                    "SUM(CASE WHEN r.estado NOT IN :noEstados THEN 1 ELSE 0 END), " +
                    "SUM(CASE WHEN r.estado IN :cancelados THEN 1 ELSE 0 END)" +
                    ") " +
                    "FROM Renta r " +
                    "WHERE r.fecha BETWEEN :start AND :end " +
                    "GROUP BY r.fecha " +
                    "ORDER BY r.fecha";
            TypedQuery<mx.avanti.desarollo.dashboard.DailyRentaStatusDTO> q =
                    em.createQuery(jpql, mx.avanti.desarollo.dashboard.DailyRentaStatusDTO.class);
            java.util.Set<String> noEstados = new java.util.HashSet<>();
            noEstados.addAll(EstadosConfig.CANCELADA);
            noEstados.addAll(EstadosConfig.COTIZACIONES);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            q.setParameter("noEstados", noEstados);
            q.setParameter("cancelados", EstadosConfig.CANCELADA);
            return q.getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    // --- Top Articulos ---
    public List<TopArticuloDTO> findTopArticulosVendidos(Date start, Date end, int topN) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT new mx.avanti.desarollo.dashboard.TopArticuloDTO(a.id, a.nombre, SUM(d.cantidad)) " +
                    "FROM Detallerenta d " +
                    "JOIN d.idrenta r " +
                    "JOIN d.idarticulo a " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL) " +
                    "AND r.estado NOT IN :noEstados " +
                    "GROUP BY a.id, a.nombre " +
                    "ORDER BY SUM(d.cantidad) DESC";

            TypedQuery<TopArticuloDTO> q = em.createQuery(jpql, TopArticuloDTO.class);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            java.util.Set<String> noEstados = new java.util.HashSet<>();
            noEstados.addAll(EstadosConfig.CANCELADA);
            noEstados.addAll(EstadosConfig.COTIZACIONES);
            q.setParameter("noEstados", noEstados);
            q.setMaxResults(topN);
            return q.getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public long sumCantidadTotalArticulosVendidos(Date start, Date end) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COALESCE(SUM(d.cantidad), 0) " +
                    "FROM Detallerenta d " +
                    "JOIN d.idrenta r " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL) " +
                    "AND r.estado NOT IN :noEstados";
            Query q = em.createQuery(jpql);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            java.util.Set<String> noEstados = new java.util.HashSet<>();
            noEstados.addAll(EstadosConfig.CANCELADA);
            noEstados.addAll(EstadosConfig.COTIZACIONES);
            q.setParameter("noEstados", noEstados);
            return ((Number) q.getSingleResult()).longValue();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Embudo de cotizaciones: creadas -> aprobadas -> pagadas.
     */
    public CotizacionFunnelDTO findCotizacionFunnel(Date start, Date end) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT " +
                    "COUNT(r), " +
                    "SUM(CASE WHEN r.estado NOT IN :solicitudes THEN 1 ELSE 0 END), " +
                    "SUM(CASE WHEN r.estado = :finalizada THEN 1 ELSE 0 END) " +
                    "FROM Renta r " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL)";

            TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            q.setParameter("solicitudes", EstadosConfig.COTIZACIONES);
            q.setParameter("finalizada", "Finalizada");

            Object[] row = q.getSingleResult();
            CotizacionFunnelDTO dto = new CotizacionFunnelDTO();
            dto.setCreadas(numberToLong(row[0]));
            dto.setAprobadas(numberToLong(row[1]));
            dto.setPagadas(numberToLong(row[2]));
            return dto;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Cotizaciones vs rentas por bucket de monto.
     */
    public List<CotizacionEfectividadDTO> findCotizacionesPorTamano(Date start, Date end) {
        EntityManager em = getEntityManager();
        try {
            String bucketCase = "CASE WHEN COALESCE(r.total, 0) < 5000 THEN 'SMALL' " +
                    "WHEN COALESCE(r.total, 0) BETWEEN 5000 AND 20000 THEN 'MEDIUM' " +
                    "ELSE 'VIP' END";

            String jpql = "SELECT " + bucketCase + ", " +
                    "SUM(CASE WHEN r.estado IN :cotizadas THEN 1 ELSE 0 END), " +
                    "SUM(CASE WHEN r.estado IN :rentadas THEN 1 ELSE 0 END) " +
                    "FROM Renta r " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL) " +
                    "GROUP BY " + bucketCase +
                    " ORDER BY 1";

            Query q = em.createQuery(jpql);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));

            java.util.Set<String> cotizadasEstados = new java.util.HashSet<>();
            cotizadasEstados.addAll(EstadosConfig.COTIZACIONES);
            cotizadasEstados.add(EstadosConfig.PENDIENTE_PAGO);
            cotizadasEstados.addAll(EstadosConfig.EXITO);
            cotizadasEstados.addAll(EstadosConfig.CANCELADA);
            q.setParameter("cotizadas", cotizadasEstados);
            q.setParameter("rentadas", EstadosConfig.EXITO);

            List<Object[]> rows = q.getResultList();
            List<CotizacionEfectividadDTO> list = new java.util.ArrayList<>();
            for (Object[] row : rows) {
                String bucket = row[0] != null ? row[0].toString() : "SMALL";
                long cot = numberToLong(row[1]);
                long rent = numberToLong(row[2]);
                list.add(new CotizacionEfectividadDTO(bucket, cot, rent));
            }
            return list;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Montos cotizados frente a montos cerrados (pagados).
     */
    public CotizacionMontosDTO sumMontosCotizaciones(Date start, Date end) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT " +
                    "COALESCE(SUM(r.total), 0), " +
                    "COALESCE(SUM(CASE WHEN r.estado IN :pagadas THEN r.total ELSE 0 END), 0) " +
                    "FROM Renta r " +
                    "WHERE (r.fecha BETWEEN :start AND :end OR r.fecha IS NULL)";

            Query q = em.createQuery(jpql);
            q.setParameter("start", toLocalDate(start));
            q.setParameter("end", toLocalDate(end));
            q.setParameter("pagadas", EstadosConfig.EXITO);

            Object[] row = (Object[]) q.getSingleResult();
            CotizacionMontosDTO dto = new CotizacionMontosDTO();
            dto.setMontoCotizado(toBigDecimal(row[0]));
            dto.setMontoCerrado(toBigDecimal(row[1]));
            return dto;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    private long numberToLong(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return Long.parseLong(val.toString());
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return new BigDecimal(val.toString());
        return new BigDecimal(val.toString());
    }

    private java.sql.Date toSqlDate(java.util.Date date) {
        if (date == null) return null;
        return new java.sql.Date(date.getTime());
    }

    /**
     * Total de clientes registrados (no depende de fechas, nombres sin duplicar).
     */
    public long countClientesTotales(Date start, Date end) {
        EntityManager em = getEntityManager();
        try {
            Long total = em.createQuery(
                            "SELECT COUNT(DISTINCT LOWER(c.nombre)) FROM Cliente c",
                            Long.class)
                    .getSingleResult();
            return total != null ? total : 0L;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Clientes cuyo primer registro de renta valido cae en el rango.
     */
    public long countClientesNuevos(Date start, Date end) {
        EntityManager em = getEntityManager();
        try {
            LocalDate inicio = toLocalDate(start);
            LocalDate fin = toLocalDate(end);
            if (inicio == null || fin == null) return 0L;

            // Contar nombres unicos (case-insensitive) cuyo primer registro cae en el rango
            List<String> nuevos = em.createQuery(
                            "SELECT LOWER(r.idCliente.nombre) " +
                                    "FROM Renta r " +
                                    "WHERE r.idCliente IS NOT NULL " +
                                    "GROUP BY LOWER(r.idCliente.nombre) " +
                                    "HAVING MIN(r.fecha) BETWEEN :start AND :end",
                            String.class)
                    .setParameter("start", inicio)
                    .setParameter("end", fin)
                    .getResultList();
            return nuevos.size();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Cuenta clientes frecuentes (mas de una renta) en el rango seleccionado,
     * excluyendo solo estados de cotizacion/solicitud.
     */
    public long countClientesFrecuentes(java.util.Date start, java.util.Date end) {
        EntityManager em = getEntityManager();
        try {
            LocalDate inicio = toLocalDate(start);
            LocalDate fin = toLocalDate(end);
            if (inicio == null || fin == null) return 0L;

            java.util.Set<String> noEstados = new java.util.HashSet<>();
            noEstados.addAll(EstadosConfig.COTIZACIONES);
            noEstados.addAll(EstadosConfig.CANCELADA);

            List<String> frecuentes = em.createQuery(
                            "SELECT LOWER(r.idCliente.nombre) " +
                                    "FROM Renta r " +
                                    "WHERE r.idCliente IS NOT NULL " +
                                    "AND r.idCliente.nombre IS NOT NULL " +
                                    "AND r.fecha BETWEEN :start AND :end " +
                                    "AND r.estado NOT IN :noEstados " +
                                    "GROUP BY LOWER(r.idCliente.nombre) " +
                                    "HAVING COUNT(r) > 1",
                            String.class)
                    .setParameter("start", inicio)
                    .setParameter("end", fin)
                    .setParameter("noEstados", noEstados)
                    .getResultList();

            return frecuentes != null ? frecuentes.size() : 0L;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public List<ClienteGrowthPoint> findGrowthClientesMensual(LocalDate inicioMes, LocalDate finMes) {
        EntityManager em = getEntityManager();
        try {
            List<Object[]> primeras = em.createQuery(
                            "SELECT LOWER(r.idCliente.nombre), MIN(r.fecha) " +
                                    "FROM Renta r " +
                                    "WHERE r.idCliente IS NOT NULL " +
                                    "GROUP BY LOWER(r.idCliente.nombre)",
                            Object[].class)
                    .getResultList();

            java.util.Map<String, LocalDate> primeraFechaPorNombre = new java.util.HashMap<>();
            for (Object[] row : primeras) {
                if (row == null || row.length < 2) continue;
                String nombre = (String) row[0];
                LocalDate fecha = (LocalDate) row[1];
                if (nombre == null || fecha == null) continue;
                primeraFechaPorNombre.put(nombre, fecha);
            }

            long baseAnterior = primeraFechaPorNombre.values().stream()
                    .filter(d -> d != null && d.isBefore(inicioMes))
                    .count();

            java.util.Map<LocalDate, Long> altasPorDia = new java.util.HashMap<>();
            for (LocalDate d : primeraFechaPorNombre.values()) {
                if (d == null) continue;
                if (!d.isBefore(inicioMes) && !d.isAfter(finMes)) {
                    altasPorDia.put(d, altasPorDia.getOrDefault(d, 0L) + 1);
                }
            }

            List<ClienteGrowthPoint> puntos = new java.util.ArrayList<>();
            long acumulado = baseAnterior;
            LocalDate cursor = inicioMes;
            while (!cursor.isAfter(finMes)) {
                acumulado += altasPorDia.getOrDefault(cursor, 0L);
                puntos.add(new ClienteGrowthPoint(cursor, acumulado));
                cursor = cursor.plusDays(1);
            }
            return puntos;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public List<ClienteFrecuenciaDTO> findClientesFrecuentes(int topN) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT new mx.avanti.desarollo.dashboard.ClienteFrecuenciaDTO(r.idCliente.nombre, COUNT(r)) " +
                    "FROM Renta r " +
                    "WHERE r.idCliente IS NOT NULL " +
                    "GROUP BY r.idCliente.nombre " +
                    "ORDER BY COUNT(r) DESC";
            return em.createQuery(jpql, ClienteFrecuenciaDTO.class)
                    .setMaxResults(topN)
                    .getResultList();
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }
}



