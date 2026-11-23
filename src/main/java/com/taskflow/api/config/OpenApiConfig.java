package com.taskflow.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentacion OpenAPI generada por SpringDoc.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Construye el objeto OpenAPI del contrato TaskFlow.
     *
     * @return definicion OpenAPI con el titulo y la version del contrato
     */
    @Bean
    public OpenAPI taskFlowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("TaskFlow Unified API Contract")
                .version("1.0.0")
                .description("Contrato OpenAPI 3.1 compartido por todo el ecosistema TaskFlow."));
    }
}