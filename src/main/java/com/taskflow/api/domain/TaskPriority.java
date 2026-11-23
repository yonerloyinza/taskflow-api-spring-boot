package com.taskflow.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Prioridad de una tarea del ecosistema TaskFlow. Coincide con los valores del
 * tipo enumerado {@code TaskPriority} de la base de datos y del contrato OpenAPI.
 */
public enum TaskPriority {

    /**
     * Prioridad baja.
     */
    BAJA("baja"),

    /**
     * Prioridad media.
     */
    MEDIA("media"),

    /**
     * Prioridad alta.
     */
    ALTA("alta");

    private final String valor;

    /**
     * Crea la prioridad con su representacion en base de datos.
     *
     * @param valor valor almacenado en la columna de prioridad
     */
    TaskPriority(final String valor) {
        this.valor = valor;
    }

    /**
     * Devuelve la representacion almacenada en la base de datos.
     *
     * @return representacion en base de datos
     */
    @JsonValue
    public String getValor() {
        return valor;
    }

    /**
     * Convierte un valor de base de datos en su prioridad correspondiente.
     *
     * @param valor representacion en base de datos
     * @return prioridad de tarea equivalente
     * @throws IllegalArgumentException si el valor no es valido
     */
    @JsonCreator
    public static TaskPriority fromValor(final String valor) {
        for (final TaskPriority priority : values()) {
            if (priority.valor.equals(valor)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Prioridad de tarea invalida: " + valor);
    }
}
