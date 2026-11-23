package com.taskflow.api.web;

import java.time.OffsetDateTime;

/**
 * Cuerpo de la respuesta de salud del contrato OpenAPI 3.1.
 *
 * @param status estado del servicio
 * @param service nombre del servicio
 * @param database estado de la conexion a PostgreSQL
 * @param timestamp instante UTC de la comprobacion
 */
public record HealthResponse(
        String status,
        String service,
        String database,
        OffsetDateTime timestamp) {
}