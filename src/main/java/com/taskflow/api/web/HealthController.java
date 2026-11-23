package com.taskflow.api.web;

import com.taskflow.api.exception.ErrorResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Comprueba el estado del servicio y la conexion real a PostgreSQL.
 */
@RestController
public class HealthController {

    private static final String PING_QUERY = "SELECT 1";

    private static final String DATABASE_UP = "up";

    private static final String STATUS_OK = "ok";

    private static final String DATABASE_ERROR = "Database connection failed";

    private final JdbcTemplate jdbcTemplate;

    private final String service;

    /**
     * Crea el controlador de salud.
     *
     * @param jdbcTemplate acceso JDBC usado para verificar la conexion
     * @param service nombre del servicio que se reporta en la respuesta
     */
    public HealthController(final JdbcTemplate jdbcTemplate,
            @Value("${taskflow.service-name:taskflow-api-springboot}") final String service) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = service;
    }

    /**
     * Responde 200 con database up o 503 con ErrorResponse si PostgreSQL no responde.
     *
     * @return estado del servicio y de la base de datos
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> health() {
        final Instant now = Instant.now();
        try {
            jdbcTemplate.queryForObject(PING_QUERY, Integer.class);
        } catch (final RuntimeException exception) {
            final ErrorResponse body = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                    HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(), DATABASE_ERROR,
                    now.atOffset(ZoneOffset.UTC), "/health");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
        final HealthResponse body = new HealthResponse(STATUS_OK, service, DATABASE_UP,
                now.atOffset(ZoneOffset.UTC));
        return ResponseEntity.ok(body);
    }
}