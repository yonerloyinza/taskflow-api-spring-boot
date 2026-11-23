package com.taskflow.api.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre {@link TaskStatus} y su representacion de texto en la
 * base de datos. Se aplica automaticamente a todos los campos de este tipo.
 */
@Converter(autoApply = true)
public class TaskStatusConverter implements AttributeConverter<TaskStatus, String> {

    @Override
    public String convertToDatabaseColumn(final TaskStatus status) {
        return status != null ? status.getValor() : null;
    }

    @Override
    public TaskStatus convertToEntityAttribute(final String valor) {
        return valor != null ? TaskStatus.fromValor(valor) : null;
    }
}
