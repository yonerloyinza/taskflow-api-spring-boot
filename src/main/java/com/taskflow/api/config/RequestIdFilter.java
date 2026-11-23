package com.taskflow.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Asigna un identificador de peticion a cada llamada y lo expone en la respuesta.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Request-ID";

    private static final String MDC_KEY = "requestId";

    /**
     * Crea el filtro de trazabilidad.
     */
    public RequestIdFilter() {
        super();
    }

    /**
     * Propaga el identificador de peticion durante toda la cadena.
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
        final String requestId = resolveRequestId(request);
        MDC.put(MDC_KEY, requestId);
        response.setHeader(HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String resolveRequestId(final HttpServletRequest request) {
        final String incoming = request.getHeader(HEADER);
        if (StringUtils.hasText(incoming)) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }
}