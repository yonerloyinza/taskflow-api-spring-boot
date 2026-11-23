package com.taskflow.api.mapper;

import com.taskflow.api.domain.Task;
import com.taskflow.api.domain.TaskPriority;
import com.taskflow.api.domain.TaskStatus;
import com.taskflow.api.dto.CreateTaskRequest;
import com.taskflow.api.dto.PageMeta;
import com.taskflow.api.dto.PaginatedTasksResponse;
import com.taskflow.api.dto.TaskDto;
import com.taskflow.api.dto.UpdateTaskRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Convierte entre entidades {@link Task} y sus DTOs de la API. Las fechas se
 * expresan en la API con zona horaria UTC, mientras que en la base de datos
 * se almacenan como marcas de tiempo sin zona.
 */
@Component
public class TaskMapper {

    /**
     * Convierte una solicitud de creacion en una entidad. No asigna el
     * identificador ni las marcas de tiempo; corresponden al servicio.
     *
     * @param request solicitud de creacion de tarea
     * @param task    entidad destino a poblar
     */
    public void toEntity(final CreateTaskRequest request, final Task task) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.PENDIENTE);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIA);
        task.setAssignee(request.assignee());
        task.setDueDate(request.dueDate() != null ? request.dueDate().toLocalDateTime() : null);
    }

    /**
     * Convierte una entidad en su DTO de respuesta.
     *
     * @param task entidad de tarea
     * @return DTO de tarea
     */
    public TaskDto toDto(final Task task) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignee(),
                task.getDueDate() != null ? task.getDueDate().atOffset(ZoneOffset.UTC) : null,
                task.getCreatedAt() != null ? task.getCreatedAt().atOffset(ZoneOffset.UTC) : null,
                task.getUpdatedAt() != null ? task.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);
    }

    /**
     * Convierte una lista de entidades en su lista de DTOs.
     *
     * @param tasks lista de entidades de tarea
     * @return lista de DTOs de tarea
     */
    public List<TaskDto> toDtoList(final List<Task> tasks) {
        final List<TaskDto> dtos = new ArrayList<>(tasks.size());
        for (final Task task : tasks) {
            dtos.add(toDto(task));
        }
        return dtos;
    }

    /**
     * Aplica una actualizacion parcial sobre una entidad existente. Solo se
     * modifican los campos enviados de forma no nula en la solicitud.
     *
     * @param request solicitud de actualizacion parcial
     * @param task    entidad destino a actualizar
     */
    public void applyPartialUpdate(final UpdateTaskRequest request, final Task task) {
        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.assignee() != null) {
            task.setAssignee(request.assignee());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate().toLocalDateTime());
        }
    }

    /**
     * Construye la respuesta paginada a partir de una pagina de resultados.
     *
     * @param page   pagina de entidades de tarea
     * @param page   numero de pagina actual (a partir de 1)
     * @param limit  tamanio de pagina solicitado
     * @return respuesta paginada con datos y metadatos
     */
    public PaginatedTasksResponse toPaginatedResponse(final Page<Task> pageResult, final int pageNumber,
            final int limit) {
        final List<TaskDto> data = toDtoList(pageResult.getContent());
        final PageMeta meta = new PageMeta(
                (int) pageResult.getTotalElements(),
                pageNumber,
                limit,
                pageResult.getTotalPages());
        return new PaginatedTasksResponse(data, meta);
    }
}
