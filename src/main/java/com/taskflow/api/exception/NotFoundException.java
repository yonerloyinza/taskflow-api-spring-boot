package com.taskflow.api.exception;

/**
 * Senaliza que el recurso solicitado no existe.
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con el mensaje que se devolvera al cliente.
     *
     * @param message descripcion del recurso no encontrado
     */
    public NotFoundException(final String message) {
        super(message);
    }
}