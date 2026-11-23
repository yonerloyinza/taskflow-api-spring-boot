package com.taskflow.api.web;

import com.taskflow.api.exception.ErrorResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serializa los errores que Spring no delega a un controlador concreto.
 */
@RestController
public class ApiErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    private static final String GENERIC_ERROR = "Error";

    /**
     * Crea el controlador de errores.
     */
    public ApiErrorController() {
        super();
    }

    /**
     * Construye un ErrorResponse a partir de los atributos de error del contenedor.
     *
     * @param request peticion HTTP que desencadeno el error
     * @return respuesta HTTP con el mismo codigo y formato ErrorResponse
     */
    @RequestMapping(ERROR_PATH)
    public ResponseEntity<ErrorResponse> handleError(final HttpServletRequest request) {
        final int statusCode = resolveStatusCode(request);
        final String reason = resolveReason(statusCode);
        final ErrorResponse body = new ErrorResponse(statusCode, reason, reason,
                Instant.now().atOffset(ZoneOffset.UTC), resolvePath(request));
        return ResponseEntity.status(statusCode).body(body);
    }

    private int resolveStatusCode(final HttpServletRequest request) {
        final Object attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (attribute instanceof Integer value) {
            return value;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private String resolveReason(final int statusCode) {
        final HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            return GENERIC_ERROR;
        }
        return status.getReasonPhrase();
    }

    private String resolvePath(final HttpServletRequest request) {
        final Object attribute = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (attribute instanceof String value && !value.isEmpty()) {
            return value;
        }
        return request.getRequestURI();
    }
}