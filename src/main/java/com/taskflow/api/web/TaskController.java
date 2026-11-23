package com.taskflow.api.web;

import com.taskflow.api.dto.CreateTaskRequest;
import com.taskflow.api.dto.PaginatedTasksResponse;
import com.taskflow.api.dto.TaskDto;
import com.taskflow.api.dto.UpdateTaskRequest;
import com.taskflow.api.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.Validated;

/**
 * Controlador REST de tareas. Implementa los endpoints de
 * {@code /tasks} definidos en el contrato OpenAPI 3.1, incluyendo creacion,
 * listado con filtros y paginacion, detalle, actualizacion parcial y
 * eliminacion.
 */
@RestController
@RequestMapping("/tasks")
@Validated
public class TaskController {

    private final TaskService taskService;

    /**
     * Crea el controlador de tareas.
     *
     * @param taskService servicio de negocios de tareas
     */
    public TaskController(final TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Crea una nueva tarea.
     *
     * @param request solicitud de creacion de tarea
     * @return DTO de la tarea creada con estado 201
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@Valid CreateTaskRequest request) {
        return taskService.create(request);
    }

    /**
     * Lista tareas aplicando filtros opcionales y paginacion.
     *
     * @param status   filtro por estado de tarea (opcional)
     * @param priority filtro por prioridad de tarea (opcional)
     * @param assignee filtro por responsable (opcional)
     * @param page     numero de pagina (a partir de 1)
     * @param limit    tamanio de pagina (entre 1 y 100)
     * @return respuesta paginada con los datos y metadatos
     */
    @GetMapping
    public PaginatedTasksResponse list(
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final String priority,
            @RequestParam(required = false) final String assignee,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) final int limit) {
        return taskService.list(status, priority, assignee, page, limit);
    }

    /**
     * Obtiene el detalle de una tarea por su identificador.
     *
     * @param id identificador de la tarea
     * @return DTO de la tarea encontrada
     */
    @GetMapping("/{id}")
    public TaskDto getById(@PathVariable final String id) {
        return taskService.getById(id);
    }

    /**
     * Actualiza parcialmente una tarea existente.
     *
     * @param id      identificador de la tarea
     * @param request solicitud de actualizacion parcial
     * @return DTO de la tarea actualizada
     */
    @PatchMapping("/{id}")
    public TaskDto updatePartial(@PathVariable final String id, @Valid UpdateTaskRequest request) {
        return taskService.updatePartial(id, request);
    }

    /**
     * Elimina una tarea por su identificador.
     *
     * @param id identificador de la tarea
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final String id) {
        taskService.delete(id);
    }
}
