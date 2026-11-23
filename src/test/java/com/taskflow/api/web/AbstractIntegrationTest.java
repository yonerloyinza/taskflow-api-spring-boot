package com.taskflow.api.web;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Clase base para pruebas de integración que requieren una base de datos
 * PostgreSQL real mediante Testcontainers. El contenedor se inicia en un
 * bloque estático para garantizar que esté disponible antes de que Spring
 * evalúe las propiedades dinámicas del contexto.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    /** Contenedor PostgreSQL compartido por las pruebas de integración. */
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("taskflow_test")
            .withUsername("taskflow")
            .withPassword("taskflow");

    static {
        POSTGRES.start();
    }

    /**
     * Registra las propiedades de conexion apuntando al contenedor.
     *
     * @param registry registro de propiedades dinamicas del contexto
     */
    @DynamicPropertySource
    static void registrarPropiedades(final DynamicPropertyRegistry registry) {
        final String jdbcUrl = POSTGRES.getJdbcUrl();
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> POSTGRES.getUsername());
        registry.add("spring.datasource.password", () -> POSTGRES.getPassword());
        registry.add("spring.flyway.url", () -> jdbcUrl);
        registry.add("spring.flyway.user", () -> POSTGRES.getUsername());
        registry.add("spring.flyway.password", () -> POSTGRES.getPassword());
        registry.add("springdoc.swagger-ui.enabled", () -> "false");
    }
}
