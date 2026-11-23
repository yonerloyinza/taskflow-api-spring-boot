package com.taskflow.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Permite cachear en el navegador los recursos de la documentacion OpenAPI.
 */
@Component
@Order(0)
public class CacheControlFilter extends OncePerRequestFilter {

    private static final String CACHE_CONTROL_HEADER = "Cache-Control";

    private static final String CACHE_CONTROL_VALUE = "public, max-age=3600";

    private static final String CACHE_CONTROL_NO_STORE = "no-store";

    private static final List<String> CACHEABLE_PREFIXES = List.of("/swagger-ui", "/v3/api-docs");

    /**
     * Sustituye el no-store de seguridad por una politica de cache en la documentacion.
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
        final String uri = request.getRequestURI();
        if (isCacheable(uri)) {
            response.setHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
        } else {
            response.setHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_NO_STORE);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isCacheable(final String uri) {
        return CACHEABLE_PREFIXES.stream().anyMatch(uri::startsWith);
    }
}