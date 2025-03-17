package app.unit;

import app.dto.SingleReceiverRequest;
import app.entity.Status;
import app.event.TaskUpdatedStatusEvent;
import app.handler.EventHandlerTask;
import app.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventHandlerTaskTest {

    @InjectMocks
    private EventHandlerTask eventHandlerTask;

    @Mock
    private EmailService emailService;

    @Test
    void handle_ShouldSendEmail() {
        // Arrange
        TaskUpdatedStatusEvent event = new TaskUpdatedStatusEvent(1L, Status.Processing);
        String expectedMessage = "Статус задачи с ID 1 изменился на Processing";
        SingleReceiverRequest expectedRequest = new SingleReceiverRequest("dima27125@yandex.ru", "Изменение статуса задачи", expectedMessage);

        // Act
        eventHandlerTask.handle(event);

        // Assert
        verify(emailService, times(1)).sendTextEmail(refEq(expectedRequest));
    }
}
