package com.taskflow.api.service;

import com.taskflow.api.domain.Task;
import com.taskflow.api.domain.TaskPriority;
import com.taskflow.api.domain.TaskRepository;
import com.taskflow.api.domain.TaskStatus;
import com.taskflow.api.dto.CreateTaskRequest;
import com.taskflow.api.dto.PageMeta;
import com.taskflow.api.dto.PaginatedTasksResponse;
import com.taskflow.api.dto.TaskDto;
import com.taskflow.api.dto.UpdateTaskRequest;
import com.taskflow.api.exception.NotFoundException;
import com.taskflow.api.mapper.TaskMapper;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

/**
 * Pruebas unitarias de la capa de servicio {@link TaskService}. Usa dobles de
 * prueba para el repositorio y el mapper, sin base de datos real.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final String ID = "task-1";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskDto taskDto;
    private CreateTaskRequest createRequest;
    private UpdateTaskRequest updateRequest;

    @BeforeEach
    void setUp() {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        task = new Task();
        task.setId(ID);
        task.setTitle("Tarea de prueba");
        task.setDescription("Descripcion de prueba");
        task.setStatus(TaskStatus.PENDIENTE);
        task.setPriority(TaskPriority.MEDIA);
        task.setAssignee("yonerloy");
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        final OffsetDateTime nowOffset = OffsetDateTime.now(ZoneOffset.UTC);
        taskDto = new TaskDto(
                ID, "Tarea de prueba", "Descripcion de prueba",
                TaskStatus.PENDIENTE, TaskPriority.MEDIA,
                "yonerloy", nowOffset, nowOffset, nowOffset);

        createRequest = new CreateTaskRequest(
                "Tarea nueva", "Descripcion nueva",
                TaskStatus.PENDIENTE, TaskPriority.ALTA,
                "yonerloy", OffsetDateTime.now(ZoneOffset.UTC).plusDays(2));

        updateRequest = new UpdateTaskRequest(
                "Tarea actualizada", null,
                TaskStatus.HECHA, null, null, null);
    }

    @Test
    void create_debeGenerarIdYTimestampsYRetornarDto() {
        final Task entidadEsperada = new Task();
        entidadEsperada.setId(ID);
        entidadEsperada.setTitle(createRequest.title());
        entidadEsperada.setStatus(TaskStatus.PENDIENTE);
        entidadEsperada.setPriority(TaskPriority.ALTA);
        entidadEsperada.setAssignee(createRequest.assignee());
        entidadEsperada.setDueDate(createRequest.dueDate().toLocalDateTime());

        org.mockito.ArgumentCaptor<Task> captor = org.mockito.ArgumentCaptor.forClass(Task.class);
        org.mockito.BDDMockito.given(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class)))
                .willAnswer(invocacion -> {
                    Task guardada = invocacion.getArgument(0);
                    guardada.setId(ID);
                    guardada.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    guardada.setUpdatedAt(guardada.getCreatedAt());
                    return guardada;
                });
        org.mockito.BDDMockito.given(taskMapper.toDto(org.mockito.ArgumentMatchers.any(Task.class)))
                .willReturn(taskDto);

        TaskDto resultado = taskService.create(createRequest);

        org.mockito.BDDMockito.then(taskRepository).should().save(org.mockito.ArgumentMatchers.any(Task.class));
        org.mockito.BDDMockito.then(taskMapper).should().toDto(org.mockito.ArgumentMatchers.any(Task.class));
        org.assertj.core.api.Assertions.assertThat(resultado).isEqualTo(taskDto);
    }

    @Test
    void list_sinFiltros_debeRetornarPaginaDeTareas() {
        org.mockito.BDDMockito.given(taskRepository.findAll(
                org.mockito.ArgumentMatchers.any(Specification.class),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1));
        org.mockito.BDDMockito.given(taskMapper.toPaginatedResponse(
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Page.class),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(new PaginatedTasksResponse(List.of(taskDto),
                        new PageMeta(1, 1, 10, 1)));

        PaginatedTasksResponse resultado = taskService.list(null, null, null, 1, 10);

        org.assertj.core.api.Assertions.assertThat(resultado.data()).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(resultado.meta().total()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(resultado.meta().page()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(resultado.meta().limit()).isEqualTo(10);
    }

    @Test
    void list_conFiltroStatus_debeAplicarFiltro() {
        org.mockito.BDDMockito.given(taskRepository.findAll(
                org.mockito.ArgumentMatchers.any(Specification.class),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(Page.empty());
        org.mockito.BDDMockito.given(taskMapper.toPaginatedResponse(
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Page.class),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(new PaginatedTasksResponse(List.of(), new PageMeta(0, 1, 10, 0)));

        PaginatedTasksResponse resultado = taskService.list("pendiente", null, null, 1, 10);

        org.assertj.core.api.Assertions.assertThat(resultado.data()).isEmpty();
        org.assertj.core.api.Assertions.assertThat(resultado.meta().total()).isEqualTo(0);
    }

    @Test
    void list_conFiltroPriority_debeAplicarFiltro() {
        org.mockito.BDDMockito.given(taskRepository.findAll(
                org.mockito.ArgumentMatchers.any(Specification.class),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(Page.empty());
        org.mockito.BDDMockito.given(taskMapper.toPaginatedResponse(
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Page.class),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(new PaginatedTasksResponse(List.of(), new PageMeta(0, 1, 10, 0)));

        PaginatedTasksResponse resultado = taskService.list(null, "alta", null, 1, 10);

        org.assertj.core.api.Assertions.assertThat(resultado.data()).isEmpty();
    }

    @Test
    void list_conFiltroAssignee_debeAplicarFiltro() {
        org.mockito.BDDMockito.given(taskRepository.findAll(
                org.mockito.ArgumentMatchers.any(Specification.class),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(Page.empty());
        org.mockito.BDDMockito.given(taskMapper.toPaginatedResponse(
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Page.class),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(new PaginatedTasksResponse(List.of(), new PageMeta(0, 1, 10, 0)));

        PaginatedTasksResponse resultado = taskService.list(null, null, "yonerloy", 1, 10);

        org.assertj.core.api.Assertions.assertThat(resultado.data()).isEmpty();
    }

    @Test
    void getById_existente_debeRetornarDto() {
        org.mockito.BDDMockito.given(taskRepository.findById(ID)).willReturn(Optional.of(task));
        org.mockito.BDDMockito.given(taskMapper.toDto(task)).willReturn(taskDto);

        TaskDto resultado = taskService.getById(ID);

        org.assertj.core.api.Assertions.assertThat(resultado).isEqualTo(taskDto);
        org.mockito.BDDMockito.then(taskRepository).should().findById(ID);
    }

    @Test
    void getById_inexistente_debeLanzarNotFoundException() {
        org.mockito.BDDMockito.given(taskRepository.findById(ID)).willReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> taskService.getById(ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Tarea no encontrada: " + ID);
    }

    @Test
    void updatePartial_existente_debeActualizarCamposNoNulosYRetornarDto() {
        org.mockito.BDDMockito.given(taskRepository.findById(ID)).willReturn(Optional.of(task));
        org.mockito.BDDMockito.given(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class)))
                .willAnswer(invocacion -> invocacion.getArgument(0));
        org.mockito.BDDMockito.given(taskMapper.toDto(org.mockito.ArgumentMatchers.any(Task.class)))
                .willReturn(taskDto);

        TaskDto resultado = taskService.updatePartial(ID, updateRequest);

        org.assertj.core.api.Assertions.assertThat(resultado).isEqualTo(taskDto);
        org.mockito.BDDMockito.then(taskMapper).should().applyPartialUpdate(updateRequest, task);
        org.mockito.BDDMockito.then(taskRepository).should().save(task);
    }

    @Test
    void updatePartial_inexistente_debeLanzarNotFoundException() {
        org.mockito.BDDMockito.given(taskRepository.findById(ID)).willReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> taskService.updatePartial(ID, updateRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_existente_debeEliminarTarea() {
        org.mockito.BDDMockito.given(taskRepository.existsById(ID)).willReturn(true);

        taskService.delete(ID);

        org.mockito.BDDMockito.then(taskRepository).should().deleteById(ID);
    }

    @Test
    void delete_inexistente_debeLanzarNotFoundException() {
        org.mockito.BDDMockito.given(taskRepository.existsById(ID)).willReturn(false);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> taskService.delete(ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void list_conFiltrosCombinados_debeAplicarTodosLosFiltros() {
        org.mockito.BDDMockito.given(taskRepository.findAll(
                org.mockito.ArgumentMatchers.any(Specification.class),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(Page.empty());
        org.mockito.BDDMockito.given(taskMapper.toPaginatedResponse(
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Page.class),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(new PaginatedTasksResponse(List.of(), new PageMeta(0, 1, 10, 0)));

        PaginatedTasksResponse resultado = taskService.list("pendiente", "media", "yonerloy", 1, 10);

        org.assertj.core.api.Assertions.assertThat(resultado.meta().total()).isEqualTo(0);
    }
}
