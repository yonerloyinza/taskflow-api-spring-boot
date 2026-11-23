# Bitácora de IA — taskflow-api-spring-boot

| Paso | Qué se pidió al agente | Qué se aceptó | Qué se corrigió manualmente |
| :--- | :--- | :--- | :--- |
| Paso 0 | Inicialización de harness independiente | Estructura aislada con contrato OpenAPI 3.1 compartido | N/A |
| Paso 3 | Implementar endpoints CRUD de Task del contrato OpenAPI 3.1 | Entidades Task, TaskStatus, TaskPriority; repositorio JPA; DTOs TaskDto, CreateTaskRequest, UpdateTaskRequest, PaginatedTasksResponse, PageMeta; mapper TaskMapper; servicio TaskService; controlador TaskController; filtros status/priority/assignee + paginación page/limit; handler IllegalArgumentException → 400 | N/A |
| Paso 4 | Agregar suite de pruebas (25 tests) | TaskServiceTest (12 unitarias con Mockito: create, list con filtros/paginación, getById, updatePartial, delete, casos de error); TaskControllerTest (15 de integración con MockMvc + Testcontainers PostgreSQL: CRUD, filtros, paginación, validación 400, 404, UUID inválido); AbstractIntegrationTest base con @ServiceConnection | N/A |
| Paso 5a | Documentación y Docker | Dockerfile multi-etapa, docker-compose con Traefik labels y healthcheck, docs/01-architecture.md, AGENTS.md | N/A |
| Paso 5b | CI/CD VPS | .github/workflows/deploy.yml (SSH → git pull + docker compose up -d --build --no-deps api-springboot + docker image prune -f), .github/workflows/ci.yml (checkstyle + test + package) | N/A |