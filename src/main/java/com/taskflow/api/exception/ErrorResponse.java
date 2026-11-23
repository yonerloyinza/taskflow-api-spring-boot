package com.taskflow.api.exception;

import java.time.OffsetDateTime;

/**
 * Cuerpo de error del contrato OpenAPI 3.1.
 *
 * @param statusCode codigo HTTP de la respuesta
 * @param error etiqueta HTTP canonica
 * @param message detalle del error o lista de detalles
 * @param timestamp instante UTC en que se produjo el error
 * @param path ruta solicitada
 */
public record ErrorResponse(
        int statusCode,
        String error,
        Object message,
        OffsetDateTime timestamp,
        String path) {
}