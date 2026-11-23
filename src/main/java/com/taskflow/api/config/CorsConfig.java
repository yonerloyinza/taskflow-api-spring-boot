package com.taskflow.api.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Politica CORS de la API.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final long MAX_AGE_SECONDS = 3600L;

    private final List<String> allowedOrigins;

    /**
     * Crea la configuracion CORS a partir de los origenes permitidos.
     *
     * @param allowedOrigins lista separada por comas; el valor "*" admite cualquier origen
     */
    public CorsConfig(@Value("${taskflow.cors.allowed-origins:*}") final String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    /**
     * Registra las reglas CORS sobre todas las rutas.
     *
     * @param registry registro de mapeos CORS
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("X-Request-ID")
                .allowCredentials(false)
                .maxAge(MAX_AGE_SECONDS);
    }
}