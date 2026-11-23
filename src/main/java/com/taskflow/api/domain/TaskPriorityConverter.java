package com.taskflow.api.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre {@link TaskPriority} y su representacion de texto en la
 * base de datos. Se aplica automaticamente a todos los campos de este tipo.
 */
@Converter(autoApply = true)
public class TaskPriorityConverter implements AttributeConverter<TaskPriority, String> {

    @Override
    public String convertToDatabaseColumn(final TaskPriority priority) {
        return priority != null ? priority.getValor() : null;
    }

    @Override
    public TaskPriority convertToEntityAttribute(final String valor) {
        return valor != null ? TaskPriority.fromValor(valor) : null;
    }
}
