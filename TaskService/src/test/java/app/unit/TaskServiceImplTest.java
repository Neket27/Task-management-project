package app.unit;

import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import app.entity.Status;
import app.entity.Task;
import app.event.TaskUpdatedStatusEvent;
import app.exception.NotFoundException;
import app.kafka.KafkaClientProducer;
import app.mapper.task.TaskMapper;
import app.repository.TaskRepository;
import app.service.Impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private KafkaClientProducer<TaskUpdatedStatusEvent> kafkaClientProducer;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(taskService, "topic", "testTopic");
    }

    @Test
    void create_ShouldSaveAndReturnTaskDto() {
        // Arrange
        CreateTaskDto dto = new CreateTaskDto("Test Task", "Description", 1L);
        Task task = new Task();
        task.setId(1L);
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setUserId(dto.userId());
        task.setStatus(Status.Active);

        TaskDto expectedDto = new TaskDto(1L, dto.title(), dto.description(), Status.Active, dto.userId());

        when(taskMapper.toEntity(dto)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(expectedDto);

        // Act
        TaskDto result = taskService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @Test
    void create_WhenCreateTaskDtoHasMissingFields_ShouldThrowException() {
        // Arrange
        CreateTaskDto dto = new CreateTaskDto(null, "Description", 1L); // title is null
        when(taskMapper.toEntity(dto)).thenThrow(new IllegalArgumentException("Title cannot be null"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.create(dto));
    }

    @Test
    void getById_WhenTaskExists_ShouldReturnTaskDto() {
        // Arrange
        Long taskId = 1L;
        Task task = new Task();
        task.setId(taskId);

        TaskDto expectedDto = new TaskDto(taskId, "Title", "Desc", Status.Active, 1L);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(expectedDto);

        // Act
        TaskDto result = taskService.getById(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @Test
    void getById_WhenTaskNotExists_ShouldThrowNotFoundException() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> taskService.getById(taskId));
    }

    @Test
    void update_WhenTaskExists_ShouldUpdateAndReturnDto() {
        // Arrange
        Long taskId = 1L;
        UpdateTaskDto dto = new UpdateTaskDto("Updated Title", "Updated Desc", Status.Completed, 2L);

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Desc");
        existingTask.setStatus(Status.Processing);
        existingTask.setUserId(1L);

        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setTitle(dto.title());
        updatedTask.setDescription(dto.description());
        updatedTask.setStatus(dto.status());
        updatedTask.setUserId(dto.userId());

        TaskDto expectedDto = new TaskDto(taskId, dto.title(), dto.description(), dto.status(), dto.userId());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskMapper.toEntity(eq(taskId), eq(dto))).thenReturn(updatedTask);
        when(taskMapper.toDto(any(Task.class))).thenReturn(expectedDto);

        // Act
        TaskDto result = taskService.update(taskId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(kafkaClientProducer).sendTo(anyString(), any(TaskUpdatedStatusEvent.class));
    }

    @Test
    void update_WhenTaskNotExists_ShouldThrowNotFoundException() {
        // Arrange
        Long taskId = 1L;
        UpdateTaskDto dto = new UpdateTaskDto("Updated Title", "Updated Desc", Status.Completed, 2L);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> taskService.update(taskId, dto));
    }

    @Test
    void update_WhenTaskStatusNotChanged_ShouldSendKafkaEvent() {
        // Arrange
        Long taskId = 1L;
        UpdateTaskDto dto = new UpdateTaskDto("Old Title", "Old Desc", Status.Processing, 1L);

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Desc");
        existingTask.setStatus(Status.Processing);
        existingTask.setUserId(1L);

        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setTitle(dto.title());
        updatedTask.setDescription(dto.description());
        updatedTask.setStatus(dto.status());
        updatedTask.setUserId(dto.userId());

        TaskDto expectedDto = new TaskDto(taskId, dto.title(), dto.description(), dto.status(), dto.userId());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskMapper.toEntity(eq(taskId), eq(dto))).thenReturn(updatedTask);
        when(taskMapper.toDto(any(Task.class))).thenReturn(expectedDto);

        // Act
        TaskDto result = taskService.update(taskId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(kafkaClientProducer).sendTo(anyString(), any(TaskUpdatedStatusEvent.class));
    }

    @Test
    void remove_WhenTaskExists_ShouldDeleteTask() {
        // Arrange
        Long taskId = 1L;
        Task task = new Task();
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        taskService.remove(taskId);

        // Assert
        verify(taskRepository).delete(task);
    }

    @Test
    void remove_WhenTaskNotExists_ShouldThrowNotFoundException() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> taskService.remove(taskId));
    }

    @Test
    void update_WhenUserNotFound_ShouldThrowNotFoundException() {
        // Arrange
        Long taskId = 1L;
        UpdateTaskDto dto = new UpdateTaskDto("Updated Title", "Updated Desc", Status.Completed, 999L);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> taskService.update(taskId, dto));
    }
}
