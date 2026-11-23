# Bitácora de IA — taskflow-api-spring-boot

| Paso | Qué se pidió al agente | Qué se aceptó | Qué se corrigió manualmente |
| :--- | :--- | :--- | :--- |
| Paso 0 | Inicialización de harness independiente | Estructura aislada con contrato OpenAPI 3.1 compartido | N/A |
| Paso 3 | Implementar endpoints CRUD de Task del contrato OpenAPI 3.1 | Entidades Task, TaskStatus, TaskPriority; repositorio JPA; DTOs TaskDto, CreateTaskRequest, UpdateTaskRequest, PaginatedTasksResponse, PageMeta; mapper TaskMapper; servicio TaskService; controlador TaskController; filtros status/priority/assignee + paginación page/limit; handler IllegalArgumentException → 400 | N/A |