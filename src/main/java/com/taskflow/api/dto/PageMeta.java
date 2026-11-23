package com.taskflow.api.dto;

/**
 * Metadatos de paginacion de un listado de tareas.
 *
 * @param total    numero total de tareas
 * @param page     numero de pagina actual (a partir de 1)
 * @param limit    tamanio de pagina solicitado
 * @param lastPage numero de ultima pagina
 */
public record PageMeta(
        int total,
        int page,
        int limit,
        int lastPage) {
}
