package com.taskflow.api.dto;

import com.taskflow.api.domain.TaskPriority;
import com.taskflow.api.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * Solicitud de creacion de una nueva tarea, segun el esquema
 * {@code CreateTaskRequest} del contrato OpenAPI 3.1.
 *
 * @param title       titulo de la tarea (obligatorio)
 * @param description descripcion opcional de la tarea
 * @param status      estado inicial de la tarea
 * @param priority    prioridad inicial de la tarea
 * @param assignee    responsable asignado (obligatorio)
 * @param dueDate     fecha limite de entrega (obligatoria)
 */
public record CreateTaskRequest(
        @NotBlank @Size(min = 3, max = 100) String title,
        @Size(max = 500) String description,
        TaskStatus status,
        TaskPriority priority,
        @NotBlank @Size(max = 100) String assignee,
        @NotNull OffsetDateTime dueDate) {
}
