# Arquitectura y Despliegue — taskflow-api-spring-boot

- **Stack:** Java 21 + Spring Boot 3.3 + Spring Data JPA + Hibernate + Flyway + Maven
- **Estrategia de Despliegue:** VPS AWS EC2 — Docker multi-etapa (maven:3.9-eclipse-temurin-21 build, eclipse-temurin:21-jre-alpine runtime) + Traefik HTTPS
- **Base de Datos Cloud Dual:** PostgreSQL gestionado en la nube, alternable entre InsForge Cloud y Supabase Cloud únicamente mediante variables de entorno (`DATABASE_URL` con pooling puerto 6543 para runtime, `DIRECT_URL` puerto 5432 para migraciones) cargadas desde `/opt/taskflow/.env.springboot` en el VPS. Regla estricta: Prohibido instalar o importar SDKs propietarios; toda persistencia debe pasar por Spring Data JPA / Hibernate.
- **Contrato API:** OpenAPI 3.1 compartido (`docs/openapi.yaml`) — rutas, DTOs, filtros (`status`, `priority`, `assignee`), paginación y formato de errores `ErrorResponse` respetados al 100%. Documentación generada via SpringDoc OpenAPI en `/docs`.
- **Health Check:** `GET /health` verificando conexión activa a PostgreSQL Cloud (JdbcTemplate / DataSource), escuchando en `0.0.0.0:3000`.
- **Middleware Stack:** Spring Boot Actuator + Spring Security — CORS (`WebMvcConfigurer`), Rate Limit (`Bucket4j`), Security Headers (`SecurityFilterChain`), Request ID, Logging (`Logback`).
- **Validación:** Bean Validation (Jakarta) + `@Valid` — validación en DTOs, `MethodArgumentNotValidException` handler global → `ErrorResponse`.
- **Documentación API:** SpringDoc OpenAPI 3 — `/v3/api-docs`, `/swagger-ui.html` (solo dev).
- **Migraciones:** Flyway — archivos SQL en `src/main/resources/db/migration`, ejecutados en startup con `DIRECT_URL`.
- **Red:** Traefik termina TLS y enruta HTTPS → puerto interno 3000 del contenedor (sin exponer puertos en el host).
- **CI/CD:** GitHub Actions (.github/workflows/deploy.yml) → SSH al VPS → `git pull` + `docker compose up -d --build --no-deps api-springboot` + `docker image prune -f` en `/opt/taskflow`. Build local en VPS, sin Docker Hub / ghcr.io.
- **Prohibido:** Monorepos, SQLite, SDKs propietarios, migraciones manuales sin Flyway, Render, SnapDeploy, registros Docker externos.