package com.taskflow.api.domain;

/**
 * Estado de una tarea del ecosistema TaskFlow. Coincide con los valores del
 * tipo enumerado {@code TaskStatus} de la base de datos y del contrato OpenAPI.
 *
 * @param valor representación almacenada en la base de datos
 */
public enum TaskStatus {

    /**
     * Tarea pendiente de iniciar.
     */
    PENDIENTE("pendiente"),

    /**
     * Tarea en curso de ejecucion.
     */
    EN_CURSO("en_curso"),

    /**
     * Tarea finalizada.
     */
    HECHA("hecha");

    private final String valor;

    /**
     * Crea el estado con su representacion en base de datos.
     *
     * @param valor valor almacenado en la columna de estado
     */
    TaskStatus(final String valor) {
        this.valor = valor;
    }

    /**
     * Devuelve la representacion almacenada en la base de datos.
     *
     * @param valor representacion en base de datos
     */
    public String getValor() {
        return valor;
    }

    /**
     * Convierte un valor de base de datos en su estado correspondiente.
     *
     * @param valor representacion en base de datos
     * @return estado de tarea equivalente
     * @throws IllegalArgumentException si el valor no es valido
     */
    public static TaskStatus fromValor(final String valor) {
        for (final TaskStatus status : values()) {
            if (status.valor.equals(valor)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de tarea invalido: " + valor);
    }
}
