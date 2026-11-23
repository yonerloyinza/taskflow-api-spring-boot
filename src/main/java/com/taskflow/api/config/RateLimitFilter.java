package com.taskflow.api.config;

import com.taskflow.api.exception.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Limitacion de tasa por direccion de cliente con Bucket4j.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String HEADER_LIMIT = "X-RateLimit-Limit";

    private static final String HEADER_REMAINING = "X-RateLimit-Remaining";

    private static final String HEADER_RETRY_AFTER = "Retry-After";

    private static final String HEADER_FORWARDED = "X-Forwarded-For";

    private final ObjectMapper objectMapper;

    private final boolean enabled;

    private final int requests;

    private final int windowSeconds;

    private final long idleEvictionMillis;

    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();

    /**
     * Crea el filtro de limitacion de tasa.
     *
     * @param objectMapper serializador JSON usado para responder 429
     * @param enabled activa o desactiva el limitador
     * @param requests numero de peticiones permitidas por ventana
     * @param windowSeconds duracion de la ventana en segundos
     * @param idleEvictionSeconds tiempo de inactividad antes de liberar un bucket
     */
    public RateLimitFilter(final ObjectMapper objectMapper,
            @Value("${taskflow.rate-limit.enabled:true}") final boolean enabled,
            @Value("${taskflow.rate-limit.requests:100}") final int requests,
            @Value("${taskflow.rate-limit.window-seconds:60}") final int windowSeconds,
            @Value("${taskflow.rate-limit.idle-eviction-seconds:600}") final int idleEvictionSeconds) {
        super();
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.requests = requests;
        this.windowSeconds = windowSeconds;
        this.idleEvictionMillis = Duration.ofSeconds(idleEvictionSeconds).toMillis();
    }

    /**
     *理 consumes un token por peticion y responde 429 cuando se agota la capacidad.
     *
     * @param request peticion HTTP entrante
     * @param response respuesta HTTP saliente
     * @param filterChain cadena de filtros
     * @throws ServletException si un filtro posterior falla
     * @throws IOException si la respuesta no puede escribirse
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }
        final Entry entry = buckets.computeIfAbsent(clientKey(request), key -> new Entry(newBucket()));
        entry.touch();
        response.setHeader(HEADER_LIMIT, String.valueOf(requests));
        if (entry.getBucket().tryConsume(1L)) {
            response.setHeader(HEADER_REMAINING, String.valueOf(entry.getBucket().getAvailableTokens()));
            filterChain.doFilter(request, response);
            return;
        }
        reject(request, response, entry);
    }

    /**
     * Libera los buckets que llevan demasiado tiempo inactivos.
     */
    @Scheduled(fixedDelayString = "${taskflow.rate-limit.cleanup-interval-ms:300000}")
    public void evictIdleBuckets() {
        final Instant threshold = Instant.now().minusMillis(idleEvictionMillis);
        buckets.entrySet().removeIf(entry -> entry.getValue().getLastAccess().isBefore(threshold));
    }

    private Bucket newBucket() {
        final Bandwidth bandwidth = Bandwidth.builder()
                .capacity(requests)
                .refillIntervally(requests, Duration.ofSeconds(windowSeconds))
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private void reject(final HttpServletRequest request, final HttpServletResponse response, final Entry entry)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HEADER_REMAINING, "0");
        response.setHeader(HEADER_RETRY_AFTER, String.valueOf(windowSeconds));
        final ErrorResponse body = new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests",
                "Rate limit exceeded", Instant.now().atOffset(ZoneOffset.UTC), request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }

    private String clientKey(final HttpServletRequest request) {
        final String forwarded = request.getHeader(HEADER_FORWARDED);
        if (StringUtils.hasText(forwarded)) {
            final String client = forwarded.split(",")[0].trim();
            if (!client.isEmpty()) {
                return client;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Par bucket en memoria con su marca de ultimo acceso.
     */
    private static final class Entry {

        private final Bucket bucket;

        private volatile Instant lastAccess;

        private Entry(final Bucket bucket) {
            this.bucket = bucket;
            this.lastAccess = Instant.now();
        }

        private Bucket getBucket() {
            return bucket;
        }

        private Instant getLastAccess() {
            return lastAccess;
        }

        private void touch() {
            lastAccess = Instant.now();
        }
    }
}