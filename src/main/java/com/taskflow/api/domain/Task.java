package com.taskflow.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de una tarea del ecosistema TaskFlow. Se persiste en la tabla
 * {@code Task} de PostgreSQL con identificador de texto generado por la
 * aplicacion.
 *
 * @param id          identificador unico de la tarea
 * @param title       titulo de la tarea
 * @param description descripcion opcional de la tarea
 * @param status      estado actual de la tarea
 * @param priority    prioridad de la tarea
 * @param assignee    responsable asignado a la tarea
 * @param dueDate     fecha límite de entrega
 * @param createdAt   instante de creacion
 * @param updatedAt   instante de ultima actualizacion
 */
@Entity
@Table(name = "Task")
public class Task {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "priority", nullable = false)
    private TaskPriority priority;

    @Column(name = "assignee", nullable = false, length = 100)
    private String assignee;

    @Column(name = "\"dueDate\"", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "\"createdAt\"", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Task() {
    }

    /**
     * Devuelve el identificador unico de la tarea.
     *
     * @return identificador de la tarea
     */
    public String getId() {
        return id;
    }

    /**
     * Asigna el identificador unico de la tarea.
     *
     * @param id identificador de la tarea
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Devuelve el titulo de la tarea.
     *
     * @return titulo de la tarea
     */
    public String getTitle() {
        return title;
    }

    /**
     * Asigna el titulo de la tarea.
     *
     * @param title titulo de la tarea
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Devuelve la descripcion de la tarea.
     *
     * @return descripcion de la tarea, o {@code null} si no tiene
     */
    public String getDescription() {
        return description;
    }

    /**
     * Asigna la descripcion de la tarea.
     *
     * @param description descripcion de la tarea
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Devuelve el estado actual de la tarea.
     *
     * @return estado de la tarea
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Asigna el estado actual de la tarea.
     *
     * @param status estado de la tarea
     */
    public void setStatus(final TaskStatus status) {
        this.status = status;
    }

    /**
     * Devuelve la prioridad de la tarea.
     *
     * @return prioridad de la tarea
     */
    public TaskPriority getPriority() {
        return priority;
    }

    /**
     * Asigna la prioridad de la tarea.
     *
     * @param priority prioridad de la tarea
     */
    public void setPriority(final TaskPriority priority) {
        this.priority = priority;
    }

    /**
     * Devuelve el responsable asignado a la tarea.
     *
     * @return responsable de la tarea
     */
    public String getAssignee() {
        return assignee;
    }

    /**
     * Asigna el responsable de la tarea.
     *
     * @param assignee responsable de la tarea
     */
    public void setAssignee(final String assignee) {
        this.assignee = assignee;
    }

    /**
     * Devuelve la fecha limite de entrega de la tarea.
     *
     * @return fecha limite de entrega
     */
    public LocalDateTime getDueDate() {
        return dueDate;
    }

    /**
     * Asigna la fecha limite de entrega de la tarea.
     *
     * @param dueDate fecha limite de entrega
     */
    public void setDueDate(final LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Devuelve el instante de creacion de la tarea.
     *
     * @return instante de creacion
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Asigna el instante de creacion de la tarea.
     *
     * @param createdAt instante de creacion
     */
    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Devuelve el instante de ultima actualizacion de la tarea.
     *
     * @return instante de ultima actualizacion
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Asigna el instante de ultima actualizacion de la tarea.
     *
     * @param updatedAt instante de ultima actualizacion
     */
    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Task task)) {
            return false;
        }
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
