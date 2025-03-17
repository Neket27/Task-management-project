package app.integration;

import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import app.entity.Status;
import app.repository.TaskRepository;
import app.service.Impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@Transactional
@EmbeddedKafka(partitions = 1, topics = {"task-updates"}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092", "port=9092"
})
public class TaskServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();

        // Настройки для datasource
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Настройки для liquibase
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/changelog-master.yml");
    }

    @Autowired
    private TaskServiceImpl taskService;

    @Autowired
    private TaskRepository taskRepository;


    @BeforeEach
    void cleanDb() {
        taskRepository.deleteAll();
    }

    @Test
    void create_ShouldPersistTaskInPostgres() {
        CreateTaskDto dto = new CreateTaskDto("Task from container", "Integration desc", 5L);

        TaskDto result = taskService.create(dto);

        assertNotNull(result.id());
        assertEquals("Task from container", result.title());

        assertEquals(1, taskRepository.count());
    }

    @Test
    void getById_ShouldRetrieveTaskFromPostgres() {
        CreateTaskDto dto = new CreateTaskDto("Task to retrieve", "Some desc", 3L);
        TaskDto savedTask = taskService.create(dto);

        TaskDto found = taskService.getById(savedTask.id());

        assertEquals(savedTask.id(), found.id());
        assertEquals("Task to retrieve", found.title());
    }

    @Test
    void update_ShouldUpdateTaskInPostgres() {
        TaskDto saved = taskService.create(new CreateTaskDto("Old title", "Old desc", 1L));

        UpdateTaskDto updateDto = new UpdateTaskDto("New title", "New desc", Status.Completed, 1L);
        TaskDto updated = taskService.update(saved.id(), updateDto);

        assertEquals("New title", updated.title());
        assertEquals(Status.Completed, updated.status());
    }

    @Test
    void getList_ShouldReturnAllTasks() {
        taskService.create(new CreateTaskDto("Task 1", "Desc 1", 1L));
        taskService.create(new CreateTaskDto("Task 2", "Desc 2", 2L));

        List<TaskDto> tasks = taskService.getList(PageRequest.of(0, 10));

        assertEquals(2, tasks.size());
    }

    @Test
    void remove_ShouldDeleteTask() {
        TaskDto saved = taskService.create(new CreateTaskDto("Task to delete", "Desc", 1L));
        taskService.remove(saved.id());
        assertEquals(0, taskRepository.count());
    }
}
