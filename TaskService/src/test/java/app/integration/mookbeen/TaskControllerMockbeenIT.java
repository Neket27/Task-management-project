package app.integration.mookbeen;

import app.controller.TaskController;
import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import app.entity.Status;
import app.entity.Task;
import app.exception.NotFoundException;
import app.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(TaskController.class)
class TaskControllerMockbeenIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void create_ShouldReturnTaskDto() throws Exception {
        // Arrange
        CreateTaskDto dto = new CreateTaskDto("Test", "Desc", 1L);
        TaskDto response = new TaskDto(1L, "Test", "Desc", Status.Active, 1L);

        when(taskService.create(any())).thenReturn(response);

        // Act
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }


    @Test
    void create_WhenTitleIsEmpty_ReturnTaskDto() throws Exception {
        CreateTaskDto dto = new CreateTaskDto("", "Desc", 1L);
        TaskDto response = new TaskDto(1L, "Test", "Desc", Status.Active, 1L);

        when(taskService.create(any())).thenReturn(response);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void get_WhenTaskExists_ShouldReturnTaskDto() throws Exception {
        // Arrange
        TaskDto response = new TaskDto(1L, "Test", "Desc", Status.Active, 1L);

        when(taskService.getById(1L)).thenReturn(response);

        // Act
        mockMvc.perform(get("/tasks/1"))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void get_WhenTaskNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getById(1L)).thenThrow(new NotFoundException(Task.class, 1L));

        // Act
        mockMvc.perform(get("/tasks/1"))
                // Assert
                .andExpect(status().isNotFound());
    }

    @Test
    void update_ShouldReturnUpdatedTaskDto() throws Exception {
        // Arrange
        UpdateTaskDto dto = new UpdateTaskDto("Updated Title", "Updated Desc", Status.Completed, 1L);
        TaskDto response = new TaskDto(1L, "Updated Title", "Updated Desc", Status.Completed, 1L);

        when(taskService.update(any(Long.class), any(UpdateTaskDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("Completed"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void list_ShouldReturnTaskDtoList() throws Exception {
        // Arrange
        TaskDto task1 = new TaskDto(1L, "Task 1", "Desc 1", Status.Active, 1L);
        TaskDto task2 = new TaskDto(2L, "Task 2", "Desc 2", Status.Completed, 1L);

        when(taskService.getList(any())).thenReturn(List.of(task1, task2));

        // Act & Assert
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }


    @Test
    void update_WhenTaskNotExists_ShouldReturnNotFound() throws Exception {
        UpdateTaskDto dto = new UpdateTaskDto("Updated Title", "Updated Desc", Status.Completed, 1L);

        when(taskService.update(any(Long.class), any(UpdateTaskDto.class)))
                .thenThrow(new NotFoundException(Task.class, 1L));

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_WhenTaskNotExists_ShouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException(Task.class, 1L))
                .when(taskService).remove(1L);

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_WhenNoTasks_ShouldReturnEmptyList() throws Exception {
        when(taskService.getList(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void get_WhenInvalidIdFormat_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/tasks/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_WithPagination_ShouldReturnPagedTasks() throws Exception {
        TaskDto task = new TaskDto(1L, "Task", "Desc", Status.Active, 1L);

        when(taskService.getList(PageRequest.of(1, 2))).thenReturn(List.of(task));

        mockMvc.perform(get("/tasks")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

}
