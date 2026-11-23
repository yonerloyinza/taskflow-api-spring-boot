package com.taskflow.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion de rutas web adicionales.
 * Redirige /docs a la UI de Swagger (/swagger-ui.html).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/docs").setViewName("redirect:/swagger-ui.html");
        registry.addViewController("/swagger-ui").setViewName("redirect:/swagger-ui.html");
    }
}