package com.taskflow.api.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio de persistencia de entidades {@link Task}. Provee operaciones
 * CRUD sobre el identificador de texto y ejecucion de especificaciones para
 * filtrados dinamicos.
 */
public interface TaskRepository extends JpaRepository<Task, String>, JpaSpecificationExecutor<Task> {
}
