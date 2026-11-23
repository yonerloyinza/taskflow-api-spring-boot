# Roadmap — taskflow-api-spring-boot

- [ ] **Paso 0 — Aclaración de contexto:** Verificar variables de entorno y puertos locales. JDK 21+, Maven 3.9+.
- [ ] **Paso 1 — Estructura Base e Infraestructura:** Scaffolding Spring Boot 3.3 (Spring Initializr), Java 21, Maven, seguridad (CORS, Bucket4j Rate Limit, Security Headers), Validation, Global Exception Handler, Actuator, SpringDoc OpenAPI.
- [ ] **Paso 2 — Persistencia y Migraciones Duales:** Spring Data JPA + Hibernate + modelo Task, dual connection (`DATABASE_URL` pooler 6543, `DIRECT_URL` 5432), migraciones Flyway aplicadas a InsForge Cloud.
- [x] **Paso 3 — Implementación del Contrato (`docs/openapi.yaml`):** Endpoints CRUD Tasks, filtros (`status`, `priority`, `assignee`), paginación (`Pageable`), manejo errores `ErrorResponse`, `GET /health` con verificación DB en `0.0.0.0:3000`.
- [x] **Paso 4 — Testing:** Suite unitarias (Service layer) + integración (Controller) con JUnit 5 + MockMvc + Testcontainers, 25 tests passing.
- [x] **Paso 5a — Documentación y Docker:** AGENTS.md actualizado, docs/01-architecture.md, Dockerfile multi-etapa (maven:3.9-eclipse-temurin-21 → eclipse-temurin:21-jre-alpine) para VPS AWS + Traefik.
- [x] **Paso 5b — CI/CD VPS:** GitHub Actions deploy.yml → SSH → `git pull` + `docker compose up -d --build --no-deps api-springboot` + `docker image prune -f` en `/opt/taskflow`. Variables en `/opt/taskflow/.env.springboot`. Sin Render, sin registros Docker externos.