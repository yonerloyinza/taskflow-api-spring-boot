package com.taskflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la API TaskFlow.
 */
@SpringBootApplication
@EnableScheduling
public class TaskFlowApiApplication {

    /**
     * Arranca el contexto de Spring Boot.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(final String[] args) {
        SpringApplication.run(TaskFlowApiApplication.class, args);
    }
}