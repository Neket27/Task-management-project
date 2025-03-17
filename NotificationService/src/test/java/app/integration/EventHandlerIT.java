package app.integration;

import app.dto.SingleReceiverRequest;
import app.entity.Status;
import app.event.TaskUpdatedStatusEvent;
import app.handler.EventHandlerTask;
import app.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"task-updates"})
public class EventHandlerIT {

    @Autowired
    private EventHandlerTask eventHandlerTask;

    @MockBean
    private EmailService emailService;

    @Test
    public void testKafkaEventProcessing() {
        TaskUpdatedStatusEvent event = new TaskUpdatedStatusEvent(1L, Status.Processing);
        eventHandlerTask.handle(event);
        verify(emailService, times(1)).sendTextEmail(any(SingleReceiverRequest.class));
    }
}