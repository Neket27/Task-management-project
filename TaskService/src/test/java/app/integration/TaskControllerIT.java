package app.integration;

import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import app.entity.Status;
import app.repository.TaskRepository;
import app.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EmbeddedKafka(partitions = 1, topics = {"task-updates"}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092", "port=9092"
})
public class TaskControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/changelog-master.yml");
    }

    @BeforeEach
    void cleanDb() {
        taskRepository.deleteAll();
    }

    @Test
    void testCreateTask() throws Exception {
        // Arrange
        CreateTaskDto createTaskDto = new CreateTaskDto("New Task", "Task Description", 1L);

        // Act
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createTaskDto)))
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("Task Description"));
    }

    @Test
    void testCreateTaskNoTitle() throws Exception {
        // Arrange
        CreateTaskDto createTaskDto = new CreateTaskDto("", "Task Description", 1L);

        // Act
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createTaskDto)))
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(""))
                .andExpect(jsonPath("$.description").value("Task Description"));
        ;
    }

    @Test
    void testUpdateTask() throws Exception {
        // Arrange
        CreateTaskDto createTaskDto = new CreateTaskDto("New Task", "Task Description", 1L);
        TaskDto taskDto = taskService.create(createTaskDto);
        UpdateTaskDto updateTaskDto = new UpdateTaskDto("Updated Task", "Updated Task Description", Status.Active, 1L);

        // Act
        mockMvc.perform(put("/tasks/{id}", taskDto.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateTaskDto)))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Task Description"));
    }

    @Test
    void testUpdateTaskWithEmptyTitle() throws Exception {
        // Arrange
        CreateTaskDto createTaskDto = new CreateTaskDto("Valid Task", "Valid Description", 1L);
        TaskDto taskDto = taskService.create(createTaskDto);
        UpdateTaskDto updateTaskDto = new UpdateTaskDto("", "Updated task description", Status.Active, 1L);

        // Act
        mockMvc.perform(put("/tasks/{id}", taskDto.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateTaskDto)))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(""))
                .andExpect(jsonPath("$.description").value("Updated task description"));
    }

    @Test
    void testUpdateTaskNotFound() throws Exception {
        // Arrange
        UpdateTaskDto updateTaskDto = new UpdateTaskDto("Updated Task", "Updated Task Description", Status.Active, 1L);

        // Act
        mockMvc.perform(put("/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateTaskDto)))
                // Assert
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void testGetTaskNotFound() throws Exception {
        // Act
        mockMvc.perform(get("/tasks/{id}", 999L))
                // Assert
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        // Act
        mockMvc.perform(delete("/tasks/{id}", 999L))
                // Assert
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void testListTasksInvalidPage() throws Exception {
        // Act
        mockMvc.perform(get("/tasks")
                        .param("page", "-1")
                        .param("size", "10"))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    private String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
