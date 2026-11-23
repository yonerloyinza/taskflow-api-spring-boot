package com.taskflow.api.dto;

import com.taskflow.api.domain.TaskPriority;
import com.taskflow.api.domain.TaskStatus;
import java.time.OffsetDateTime;

/**
 * Representacion de una tarea devuelta por la API, segun el esquema
 * {@code Task} del contrato OpenAPI 3.1.
 *
 * @param id          identificador unico de la tarea
 * @param title       titulo de la tarea
 * @param description descripcion opcional de la tarea
 * @param status      estado actual de la tarea
 * @param priority    prioridad de la tarea
 * @param assignee    responsable asignado a la tarea
 * @param dueDate     fecha limite de entrega
 * @param createdAt   instante de creacion
 * @param updatedAt   instante de ultima actualizacion
 */
public record TaskDto(
        String id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        String assignee,
        OffsetDateTime dueDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
