package app.unit;

import app.entity.Status;
import app.event.TaskUpdatedStatusEvent;
import app.kafka.MessageDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MessageDeserializerTest {

    @Spy
    private MessageDeserializer<TaskUpdatedStatusEvent> deserializer;

    @Test
    void deserialize_ValidData_ShouldDeserialize() throws Exception {
        // Arrange
        TaskUpdatedStatusEvent event = new TaskUpdatedStatusEvent(1L, Status.Completed);
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = objectMapper.writeValueAsBytes(event);

        doReturn(event).when(deserializer).deserialize(anyString(), any());


        // Act
        TaskUpdatedStatusEvent result = deserializer.deserialize("test-topic", data);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.taskId());
        assertEquals(Status.Completed, result.status());
    }

    @Test
    void deserialize_InvalidData_ShouldReturnNull() {
        // Arrange
        byte[] invalidData = "invalid json".getBytes(StandardCharsets.UTF_8);

        // Act
        TaskUpdatedStatusEvent result = deserializer.deserialize("test-topic", invalidData);

        // Assert
        assertNull(result);
    }
}
