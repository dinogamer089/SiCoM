-- =====================================================
-- MIGRACION COMPLETA: agregar fecha_inicio a renta
-- y reemplazar el stored procedure cambiar_estado_renta
-- para que soporte el rango [fecha_inicio, fecha].
--
-- Este script es IDEMPOTENTE: se puede correr varias
-- veces sin error (verifica con INFORMATION_SCHEMA).
--
-- INSTRUCCIONES:
--   1. Abrir en MySQL Workbench.
--   2. Ejecutar TODO el archivo (rayo amarillo).
--   3. Reiniciar el servidor de aplicaciones.
-- =====================================================

USE sicom;

SET SQL_SAFE_UPDATES = 0;


-- ============================================================
-- PARTE 1: agregar columna fecha_inicio si no existe
-- ============================================================

DROP PROCEDURE IF EXISTS _tmp_migrar_fecha_inicio;

DELIMITER $$

CREATE PROCEDURE _tmp_migrar_fecha_inicio()
BEGIN
    -- 1.1 Agregar columna solo si NO existe
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'sicom'
          AND TABLE_NAME = 'renta'
          AND COLUMN_NAME = 'fecha_inicio'
    ) THEN
        ALTER TABLE renta ADD COLUMN fecha_inicio DATE NULL AFTER fecha;
    END IF;

    -- 1.2 Copiar fecha como fecha_inicio en filas que aun la tengan NULL
    UPDATE renta
    SET fecha_inicio = fecha
    WHERE fecha_inicio IS NULL;

    -- 1.3 Hacer la columna NOT NULL solo si actualmente es NULL
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'sicom'
          AND TABLE_NAME = 'renta'
          AND COLUMN_NAME = 'fecha_inicio'
          AND IS_NULLABLE = 'YES'
    ) THEN
        ALTER TABLE renta MODIFY COLUMN fecha_inicio DATE NOT NULL;
    END IF;

    -- 1.4 Crear indice solo si no existe
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = 'sicom'
          AND TABLE_NAME = 'renta'
          AND INDEX_NAME = 'idx_renta_fecha_inicio'
    ) THEN
        CREATE INDEX idx_renta_fecha_inicio ON renta(fecha_inicio);
    END IF;
END$$

DELIMITER ;

-- Ejecutar la migracion
CALL _tmp_migrar_fecha_inicio();

-- Limpiar
DROP PROCEDURE _tmp_migrar_fecha_inicio;


-- ============================================================
-- PARTE 2: Stored procedure cambiar_estado_renta
-- ============================================================

DROP PROCEDURE IF EXISTS cambiar_estado_renta;

DELIMITER $$

CREATE PROCEDURE cambiar_estado_renta(
    IN p_idRenta  INT,
    IN p_nuevoEst VARCHAR(45)
)
BEGIN
    DECLARE v_estado_actual   VARCHAR(45);
    DECLARE v_fecha_inicio    DATE;
    DECLARE v_fecha_fin       DATE;
    DECLARE v_dia_actual      DATE;

    DECLARE v_idarticulo      INT;
    DECLARE v_cantidad        INT;
    DECLARE v_done            INT DEFAULT 0;

    DECLARE cur_detalles CURSOR FOR
        SELECT idarticulo, cantidad
        FROM detallerenta
        WHERE idrenta = p_idRenta;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado, fecha_inicio, fecha
    INTO v_estado_actual, v_fecha_inicio, v_fecha_fin
    FROM renta
    WHERE idRenta = p_idRenta;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Renta no encontrada';
    END IF;

    IF v_fecha_inicio IS NULL THEN
        SET v_fecha_inicio = v_fecha_fin;
    END IF;

    UPDATE renta
    SET estado = p_nuevoEst
    WHERE idRenta = p_idRenta;

    -- Reservar stock al aprobar/confirmar
    IF (UPPER(v_estado_actual) = 'SOLICITADA')
       AND (p_nuevoEst IN ('Aprobada', 'Confirmado'))
    THEN
        OPEN cur_detalles;
        SET v_done = 0;
        bucle_det: LOOP
            FETCH cur_detalles INTO v_idarticulo, v_cantidad;
            IF v_done = 1 THEN
                LEAVE bucle_det;
            END IF;

            SET v_dia_actual = v_fecha_inicio;
            bucle_dia: WHILE v_dia_actual <= v_fecha_fin DO
                INSERT INTO stock_reservado_diario (idarticulo, fecha, cantidad_reservada)
                VALUES (v_idarticulo, v_dia_actual, v_cantidad)
                ON DUPLICATE KEY UPDATE
                    cantidad_reservada = cantidad_reservada + v_cantidad;

                SET v_dia_actual = DATE_ADD(v_dia_actual, INTERVAL 1 DAY);
            END WHILE bucle_dia;
        END LOOP bucle_det;
        CLOSE cur_detalles;
    END IF;

    -- Liberar stock al cancelar o finalizar
    IF (p_nuevoEst IN ('Cancelada', 'Finalizada'))
       AND (UPPER(v_estado_actual) NOT IN ('SOLICITADA', 'CANCELADA', 'FINALIZADA'))
    THEN
        OPEN cur_detalles;
        SET v_done = 0;
        bucle_det2: LOOP
            FETCH cur_detalles INTO v_idarticulo, v_cantidad;
            IF v_done = 1 THEN
                LEAVE bucle_det2;
            END IF;

            SET v_dia_actual = v_fecha_inicio;
            bucle_dia2: WHILE v_dia_actual <= v_fecha_fin DO
                UPDATE stock_reservado_diario
                SET cantidad_reservada = GREATEST(cantidad_reservada - v_cantidad, 0)
                WHERE idarticulo = v_idarticulo
                  AND fecha = v_dia_actual;

                DELETE FROM stock_reservado_diario
                WHERE idarticulo = v_idarticulo
                  AND fecha = v_dia_actual
                  AND cantidad_reservada <= 0;

                SET v_dia_actual = DATE_ADD(v_dia_actual, INTERVAL 1 DAY);
            END WHILE bucle_dia2;
        END LOOP bucle_det2;
        CLOSE cur_detalles;
    END IF;

    COMMIT;
END$$

DELIMITER ;


SET SQL_SAFE_UPDATES = 1;


-- ============================================================
-- PARTE 3: Verificacion (deben devolver 1 fila cada una)
-- ============================================================

SELECT COLUMN_NAME, IS_NULLABLE, DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'sicom'
  AND TABLE_NAME = 'renta'
  AND COLUMN_NAME = 'fecha_inicio';

SELECT ROUTINE_NAME, ROUTINE_TYPE
FROM INFORMATION_SCHEMA.ROUTINES
WHERE ROUTINE_SCHEMA = 'sicom'
  AND ROUTINE_NAME = 'cambiar_estado_renta';
