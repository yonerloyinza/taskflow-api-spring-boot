package com.taskflow.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.EOFException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce las excepciones de la aplicacion al formato ErrorResponse del contrato.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String INTERNAL_MESSAGE = "Internal Server Error";

    private static final String NOT_FOUND_MESSAGE = "Not Found";

    private static final String INVALID_JSON_MESSAGE = "El cuerpo JSON no es valida";

    private static final String EMPTY_BODY_MESSAGE = "El cuerpo de la peticion esta vacio";

    private static final String INVALID_UUID_MESSAGE = "El id debe ser un UUID valido";

    /**
     * Convierte errores de validacion de DTOs en 400 con la lista de mensajes.
     *
     * @param exception excepcion lanzada por Bean Validation
     * @param request peticion HTTP en curso
     * @return respuesta 400 con formato ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(final MethodArgumentNotValidException exception,
            final HttpServletRequest request) {
        final List<String> messages = exception.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::describe)
                .toList();
        return fail(HttpStatus.BAD_REQUEST, messages, request);
    }

    /**
     * Convierte violaciones de restricciones en parametros de consulta en 400.
     *
     * @param exception excepcion lanzada por Bean Validation
     * @param request peticion HTTP en curso
     * @return respuesta 400 con formato ErrorResponse
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(final ConstraintViolationException exception,
            final HttpServletRequest request) {
        final List<String> messages = exception.getConstraintViolations().stream()
                .map(GlobalExceptionHandler::describe)
                .toList();
        return fail(HttpStatus.BAD_REQUEST, messages, request);
    }

    /**
     * Convierte cuerpos JSON ausentes o mal formados en 400.
     *
     * @param exception excepcion de lectura del cuerpo
     * @param request peticion HTTP en curso
     * @return respuesta 400 con formato ErrorResponse
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(final HttpMessageNotReadableException exception,
            final HttpServletRequest request) {
        if (exception.getRootCause() instanceof EOFException) {
            return fail(HttpStatus.BAD_REQUEST, List.of(EMPTY_BODY_MESSAGE), request);
        }
        return fail(HttpStatus.BAD_REQUEST, List.of(INVALID_JSON_MESSAGE), request);
    }

    /**
     * Convierte parametros con tipo incorrecto en 400.
     *
     * @param exception excepcion de conversion de argumentos
     * @param request peticion HTTP en curso
     * @return respuesta 400 con formato ErrorResponse
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(final MethodArgumentTypeMismatchException exception,
            final HttpServletRequest request) {
        if (exception.getRequiredType() == UUID.class) {
            return fail(HttpStatus.BAD_REQUEST, List.of(INVALID_UUID_MESSAGE), request);
        }
        return fail(HttpStatus.BAD_REQUEST, List.of("Parametro invalido: " + exception.getName()), request);
    }

    /**
     * Convierte parametros de consulta obligatorios ausentes en 400.
     *
     * @param exception excepcion de parametro ausente
     * @param request peticion HTTP en curso
     * @return respuesta 400 con formato ErrorResponse
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            final MissingServletRequestParameterException exception, final HttpServletRequest request) {
        return fail(HttpStatus.BAD_REQUEST, List.of("El parametro " + exception.getParameterName()
                + " es obligatorio"), request);
    }

    /**
     * Convierte recursos inexistentes en 404.
     *
     * @param exception excepcion de dominio
     * @param request peticion HTTP en curso
     * @return respuesta 404 con formato ErrorResponse
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(final NotFoundException exception,
            final HttpServletRequest request) {
        return fail(HttpStatus.NOT_FOUND, List.of(exception.getMessage()), request);
    }

    /**
     * Convierte rutas sin recurso estatico asociado en 404.
     *
     * @param exception excepcion de recurso estatico ausente
     * @param request peticion HTTP en curso
     * @return respuesta 404 con formato ErrorResponse
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(final NoResourceFoundException exception,
            final HttpServletRequest request) {
        return fail(HttpStatus.NOT_FOUND, List.of(NOT_FOUND_MESSAGE), request);
    }

    /**
     * Oculta los fallos de persistencia detras de un 500 generico.
     *
     * @param exception excepcion de acceso a datos
     * @param request peticion HTTP en curso
     * @return respuesta 500 con formato ErrorResponse
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(final DataAccessException exception,
            final HttpServletRequest request) {
        LOG.error("Fallo de acceso a datos en {} {}", request.getMethod(), request.getRequestURI(), exception);
        return fail(HttpStatus.INTERNAL_SERVER_ERROR, List.of(INTERNAL_MESSAGE), request);
    }

    /**
     * Captura cualquier error no previsto y responde 500 sin filtrar detalles.
     *
     * @param exception excepcion no controlada
     * @param request peticion HTTP en curso
     * @return respuesta 500 con formato ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(final Exception exception,
            final HttpServletRequest request) {
        LOG.error("Error no controlado en {} {}", request.getMethod(), request.getRequestURI(), exception);
        return fail(HttpStatus.INTERNAL_SERVER_ERROR, List.of(INTERNAL_MESSAGE), request);
    }

    private ResponseEntity<ErrorResponse> fail(final HttpStatus status, final Object message,
            final HttpServletRequest request) {
        final ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message,
                Instant.now().atOffset(ZoneOffset.UTC), request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private static String describe(final FieldError error) {
        if (StringUtils.hasText(error.getDefaultMessage())) {
            return error.getDefaultMessage();
        }
        return error.getField() + " no es valido";
    }

    private static String describe(final ConstraintViolation<?> violation) {
        if (StringUtils.hasText(violation.getMessage())) {
            return violation.getMessage();
        }
        return violation.getPropertyPath().toString() + " no es valido";
    }
}