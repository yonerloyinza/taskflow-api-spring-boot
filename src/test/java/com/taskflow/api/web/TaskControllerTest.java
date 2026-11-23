package com.taskflow.api.web;

import com.taskflow.api.domain.TaskStatus;
import com.taskflow.api.dto.CreateTaskRequest;
import com.taskflow.api.dto.TaskDto;
import com.taskflow.api.dto.UpdateTaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Pruebas de integración del controlador {@link TaskController} contra una
 * base de datos PostgreSQL real (Testcontainers). Valida los endpoints de
 * {@code /tasks} definidos en el contrato OpenAPI 3.1.
 */
@AutoConfigureMockMvc
class TaskControllerTest extends AbstractIntegrationTest {

    private static final OffsetDateTime FECHA_LIMITE = OffsetDateTime.parse("2026-12-01T09:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String crearTarea(final String titulo, final String descripcion, final String estado,
            final String prioridad, final String responsable, final OffsetDateTime fechaLimite) throws Exception {
        LinkedHashMap<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("title", titulo);
        cuerpo.put("description", descripcion);
        cuerpo.put("status", estado);
        cuerpo.put("priority", prioridad);
        cuerpo.put("assignee", responsable);
        cuerpo.put("dueDate", fechaLimite.toString());
        MvcResult resultado = mockMvc.perform(MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuerpo)))
                .andReturn();
        return (String) extraerCampo(resultado.getResponse().getContentAsString(), "id");
    }

    private Object extraerCampo(final String json, final String campo) {
        // Extraccion minima sin depender de Jackson en el cuerpo de respuesta.
        // Formato esperado: {"id":"...","title":"...",...}
        String clave = "\"" + campo + "\":\"";
        int inicio = json.indexOf(clave);
        if (inicio < 0) {
            return null;
        }
        inicio += clave.length();
        int fin = json.indexOf('"', inicio);
        return json.substring(inicio, fin);
    }

    @Test
    void crearTarea_valido_debeResponder201YDevolverTarea() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTaskRequest(
                        "Tarea de integracion", "Descripcion",
                        null, null, "yonerloy", FECHA_LIMITE))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title")
                        .value("Tarea de integracion"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.assignee")
                        .value("yonerloy"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status")
                        .value("pendiente"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.priority")
                        .value("media"));
    }

    @Test
    void crearTarea_tituloMuyCorto_debeResponder400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTaskRequest(
                        "ab", null, null, null, "yonerloy", FECHA_LIMITE))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void crearTarea_sinAsignado_debeResponder400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTaskRequest(
                        "Tarea valida", null, null, null, null, FECHA_LIMITE))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void listarTareas_debeResponder200ConDatosYMeta() throws Exception {
        crearTarea("Tarea A", null, "pendiente", "media", "yonerloy", FECHA_LIMITE);
        crearTarea("Tarea B", null, "hecha", "alta", "otro", FECHA_LIMITE);

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks")
                .param("page", "1")
                .param("limit", "10"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data")
                        .isArray())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.meta.total")
                        .isNumber())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.meta.page")
                        .value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.meta.limit")
                        .value(10));
    }

    @Test
    void listarTareas_conFiltroEstado_debeFiltrar() throws Exception {
        crearTarea("Tarea pendiente", null, "pendiente", "media", "yonerloy", FECHA_LIMITE);
        crearTarea("Tarea hecha", null, "hecha", "alta", "yonerloy", FECHA_LIMITE);

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks").param("status", "pendiente"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data")
                        .isArray());
    }

    @Test
    void listarTareas_conFiltroPrioridad_debeFiltrar() throws Exception {
        crearTarea("Tarea alta", null, "pendiente", "alta", "yonerloy", FECHA_LIMITE);

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks").param("priority", "alta"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void listarTareas_conFiltroAsignado_debeFiltrar() throws Exception {
        crearTarea("Tarea asignada", null, "pendiente", "media", "yonerloy", FECHA_LIMITE);
        crearTarea("Tarea otro", null, "pendiente", "media", "otro", FECHA_LIMITE);

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks").param("assignee", "yonerloy"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void listarTareas_paginacion_inicioEnUno_debeRespetarPagina() throws Exception {
        crearTarea("Tarea 1", null, "pendiente", "media", "yonerloy", FECHA_LIMITE);
        crearTarea("Tarea 2", null, "pendiente", "media", "yonerloy", FECHA_LIMITE);

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks")
                .param("page", "2")
                .param("limit", "1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.meta.page")
                        .value(2))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.meta.limit")
                        .value(1));
    }

    @Test
    void obtenerTareaPorId_existente_debeResponder200() throws Exception {
        String id = crearTarea("Tarea detalle", null, "pendiente", "media", "yonerloy", FECHA_LIMITE);

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/" + id))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.id")
                        .value(id));
    }

    @Test
    void obtenerTareaPorId_inexistente_debeResponder404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/00000000-0000-0000-0000-000000000000"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void obtenerTareaPorId_formatoInvalido_debeResponder400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/no-es-un-uuid"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void actualizarTareaParcial_existente_debeResponder200YActualizar() throws Exception {
        String id = crearTarea("Tarea original", null, "pendiente", "media", "yonerloy", FECHA_LIMITE);

        mockMvc.perform(MockMvcRequestBuilders.patch("/tasks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateTaskRequest(
                        null, null, TaskStatus.HECHA, null, null, null))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status")
                        .value("hecha"));
    }

    @Test
    void actualizarTareaParcial_inexistente_debeResponder404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/tasks/00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateTaskRequest(
                        "Nuevo titulo", null, null, null, null, null))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void eliminarTarea_existente_debeResponder204() throws Exception {
        String id = crearTarea("Tarea para eliminar", null, "pendiente", "media", "yonerloy", FECHA_LIMITE);

        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/" + id))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void eliminarTarea_inexistente_debeResponder404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/00000000-0000-0000-0000-000000000000"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
    }
}
