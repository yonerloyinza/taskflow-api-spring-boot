package com.taskflow.api.service;

import com.taskflow.api.domain.Task;
import com.taskflow.api.domain.TaskPriority;
import com.taskflow.api.domain.TaskRepository;
import com.taskflow.api.domain.TaskStatus;
import com.taskflow.api.dto.CreateTaskRequest;
import com.taskflow.api.dto.PaginatedTasksResponse;
import com.taskflow.api.dto.TaskDto;
import com.taskflow.api.dto.UpdateTaskRequest;
import com.taskflow.api.exception.NotFoundException;
import com.taskflow.api.mapper.TaskMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

/**
 * Servicio de negocios de tareas. Orquesta la creacion, consulta, actualizacion
 * parcial y eliminacion de tareas sobre el repositorio de persistencia.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    /**
     * Crea el servicio de tareas.
     *
     * @param taskRepository repositorio de persistencia de tareas
     * @param taskMapper     conversor entre entidades y DTOs
     */
    public TaskService(final TaskRepository taskRepository, final TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    /**
     * Crea una nueva tarea. Genera el identificador y las marcas de tiempo de
     * creacion y actualizacion.
     *
     * @param request solicitud de creacion de tarea
     * @return DTO de la tarea creada
     */
    @Transactional
    public TaskDto create(final CreateTaskRequest request) {
        final Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        taskMapper.toEntity(request, task);
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        final Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    /**
     * Lista tareas aplicando filtros opcionales y paginacion. La pagina se
     * numera a partir de 1.
     *
     * @param status   filtro por estado de tarea (opcional)
     * @param priority filtro por prioridad de tarea (opcional)
     * @param assignee filtro por responsable (opcional)
     * @param page     numero de pagina (a partir de 1)
     * @param limit    tamanio de pagina (entre 1 y 100)
     * @return respuesta paginada con los datos y metadatos
     */
    @Transactional(readOnly = true)
    public PaginatedTasksResponse list(final String status, final String priority, final String assignee,
            final int page, final int limit) {
        final Specification<Task> specification = (root, query, criteriaBuilder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), TaskStatus.fromValor(status)));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), TaskPriority.fromValor(priority)));
            }
            if (assignee != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee"), assignee));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        final Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        final Page<Task> pageResult = taskRepository.findAll(specification, pageable);
        return taskMapper.toPaginatedResponse(pageResult, page, limit);
    }

    /**
     * Obtiene el detalle de una tarea por su identificador.
     *
     * @param id identificador de la tarea
     * @return DTO de la tarea encontrada
     * @throws NotFoundException si no existe la tarea
     */
    @Transactional(readOnly = true)
    public TaskDto getById(final String id) {
        final Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada: " + id));
        return taskMapper.toDto(task);
    }

    /**
     * Actualiza parcialmente una tarea existente. Solo se modifican los campos
     * enviados de forma no nula en la solicitud. Actualiza la marca de
     * actualizacion al instante actual.
     *
     * @param id      identificador de la tarea
     * @param request solicitud de actualizacion parcial
     * @return DTO de la tarea actualizada
     * @throws NotFoundException si no existe la tarea
     */
    @Transactional
    public TaskDto updatePartial(final String id, final UpdateTaskRequest request) {
        final Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada: " + id));
        taskMapper.applyPartialUpdate(request, task);
        task.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        final Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    /**
     * Elimina una tarea por su identificador.
     *
     * @param id identificador de la tarea
     * @throws NotFoundException si no existe la tarea
     */
    @Transactional
    public void delete(final String id) {
        if (!taskRepository.existsById(id)) {
            throw new NotFoundException("Tarea no encontrada: " + id);
        }
        taskRepository.deleteById(id);
    }
}
