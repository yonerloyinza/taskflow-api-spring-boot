package com.taskflow.api.dto;

import java.util.List;

/**
 * Respuesta paginada de listado de tareas, segun el esquema
 * {@code PaginatedTasksResponse} del contrato OpenAPI 3.1.
 *
 * @param data lista de tareas de la pagina solicitada
 * @param meta metadatos de paginacion
 */
public record PaginatedTasksResponse(
        List<TaskDto> data,
        PageMeta meta) {
}
