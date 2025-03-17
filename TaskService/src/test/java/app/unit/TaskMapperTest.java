package app.unit;

import static org.junit.jupiter.api.Assertions.*;

import app.dto.CreateTaskDto;
import app.dto.UpdateTaskDto;
import app.entity.Status;
import app.entity.Task;
import app.mapper.task.TaskMapper;
import app.mapper.task.TaskMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskMapperTest {

    private final TaskMapper taskMapper = new TaskMapperImpl();

    @Test
    void toEntity_FromCreateTaskDto_ShouldMapCorrectly() {
        // Arrange
        CreateTaskDto dto = new CreateTaskDto("Title", "Description", 1L);

        // Act
        Task task = taskMapper.toEntity(dto);

        // Assert
        assertNotNull(task);
        assertEquals(dto.title(), task.getTitle());
        assertEquals(dto.description(), task.getDescription());
        assertEquals(dto.userId(), task.getUserId());
    }

    @Test
    void toEntity_FromUpdateTaskDto_ShouldMapCorrectly() {
        // Arrange
        UpdateTaskDto dto = new UpdateTaskDto("Updated Title", "Updated Desc", Status.Processing, 2L);

        // Act
        Task task = taskMapper.toEntity(dto);

        // Assert
        assertNotNull(task);
        assertEquals(dto.title(), task.getTitle());
        assertEquals(dto.description(), task.getDescription());
        assertEquals(dto.status(), task.getStatus());
        assertEquals(dto.userId(), task.getUserId());
    }

    @Test
    void toEntity_WithIdAndUpdateTaskDto_ShouldMapCorrectly() {
        // Arrange
        Long taskId = 1L;
        UpdateTaskDto dto = new UpdateTaskDto("Updated Title", "Updated Desc", Status.Completed, 3L);

        // Act
        Task task = taskMapper.toEntity(taskId, dto);

        // Assert
        assertNotNull(task);
        assertEquals(taskId, task.getId());
        assertEquals(dto.title(), task.getTitle());
        assertEquals(dto.description(), task.getDescription());
        assertEquals(dto.status(), task.getStatus());
        assertEquals(dto.userId(), task.getUserId());
    }

    @Test
    void update_ShouldModifyExistingTask() {
        // Arrange
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setStatus(Status.Active);
        existingTask.setUserId(1L);

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("New Title");
        updatedTask.setDescription("New Description");
        updatedTask.setStatus(Status.Processing);
        updatedTask.setUserId(2L);

        // Act
        taskMapper.update(existingTask, updatedTask);

        // Assert
        assertEquals(1L, existingTask.getId());
        assertEquals("New Title", existingTask.getTitle());
        assertEquals("New Description", existingTask.getDescription());
        assertEquals(Status.Processing, existingTask.getStatus());
        assertEquals(2L, existingTask.getUserId());
    }

    @Test
    void toEntity_FromCreateTaskDto_WithEmptyFields_ShouldHandleCorrectly() {
        // Arrange
        CreateTaskDto dto = new CreateTaskDto("", "", 0L);

        // Act
        Task task = taskMapper.toEntity(dto);

        // Assert
        assertNotNull(task);
        assertEquals("", task.getTitle());
        assertEquals("", task.getDescription());
        assertEquals(0L, task.getUserId());
    }

    @Test
    void toEntity_FromUpdateTaskDto_WithEmptyFields_ShouldHandleCorrectly() {
        // Arrange
        UpdateTaskDto dto = new UpdateTaskDto("", "", Status.Active, 0L);

        // Act
        Task task = taskMapper.toEntity(dto);

        // Assert
        assertNotNull(task);
        assertEquals("", task.getTitle());
        assertEquals("", task.getDescription());
        assertEquals(Status.Active, task.getStatus());
        assertEquals(0L, task.getUserId());
    }

    @Test
    void toEntity_WithNullCreateTaskDto_ShouldReturnNull() {
        // Act
        Task task = taskMapper.toEntity((CreateTaskDto) null);

        // Assert
        assertNull(task);
    }

    @Test
    void toEntity_WithNullUpdateTaskDto_ShouldReturnNull() {
        // Act
        Task task = taskMapper.toEntity((UpdateTaskDto) null);

        // Assert
        assertNull(task);
    }

    @Test
    void toEntity_WithSpecialCharactersInFields_ShouldMapCorrectly() {
        // Arrange
        CreateTaskDto dto = new CreateTaskDto("Title @!#%", "Description $%^", 1L);

        // Act
        Task task = taskMapper.toEntity(dto);

        // Assert
        assertNotNull(task);
        assertEquals(dto.title(), task.getTitle());
        assertEquals(dto.description(), task.getDescription());
        assertEquals(dto.userId(), task.getUserId());
    }

    @Test
    void toEntity_WithNullFieldsInUpdateTaskDto_ShouldHandleNullValues() {
        // Arrange
        UpdateTaskDto dto = new UpdateTaskDto(null, null, Status.Completed, null);

        // Act
        Task task = taskMapper.toEntity(dto);

        // Assert
        assertNotNull(task);
        assertNull(task.getTitle());
        assertNull(task.getDescription());
        assertEquals(Status.Completed, task.getStatus());
        assertNull(task.getUserId());
    }

    @Test
    void update_ShouldChangeIdWhenUpdatingTask() {
        // Arrange
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setStatus(Status.Active);
        existingTask.setUserId(1L);

        Task updatedTask = new Task();
        updatedTask.setId(2L);
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setStatus(Status.Completed);
        updatedTask.setUserId(2L);

        // Act
        taskMapper.update(existingTask, updatedTask);

        // Assert
        assertEquals(2L, existingTask.getId());
        assertEquals("Updated Title", existingTask.getTitle());
        assertEquals("Updated Description", existingTask.getDescription());
        assertEquals(Status.Completed, existingTask.getStatus());
        assertEquals(2L, existingTask.getUserId());
    }
}
