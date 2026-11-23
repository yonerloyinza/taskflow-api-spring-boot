package com.taskflow.api.dto;

import com.taskflow.api.domain.TaskPriority;
import com.taskflow.api.domain.TaskStatus;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * Solicitud de actualizacion parcial de una tarea, segun el esquema
 * {@code UpdateTaskRequest} del contrato OpenAPI 3.1. Todos los campos son
 * opcionales: solo se actualizan los que se envian en el cuerpo.
 *
 * @param title       nuevo titulo de la tarea
 * @param description nueva descripcion de la tarea
 * @param status      nuevo estado de la tarea
 * @param priority    nueva prioridad de la tarea
 * @param assignee    nuevo responsable de la tarea
 * @param dueDate     nueva fecha limite de entrega
 */
public record UpdateTaskRequest(
        @Size(min = 3, max = 100) String title,
        @Size(max = 500) String description,
        TaskStatus status,
        TaskPriority priority,
        @Size(max = 100) String assignee,
        OffsetDateTime dueDate) {
}
