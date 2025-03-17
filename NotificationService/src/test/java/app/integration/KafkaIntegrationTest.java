package app.integration;

import app.dto.SingleReceiverRequest;
import app.entity.Status;
import app.event.TaskUpdatedStatusEvent;
import app.service.EmailService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"task-updates"}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092", "port=9092"
})
public class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, TaskUpdatedStatusEvent> kafkaTemplate;

    @Autowired
    private EmailService emailService;

    @Test
    public void testKafkaEventProcessing() throws Exception {
        TaskUpdatedStatusEvent event = new TaskUpdatedStatusEvent(1L, Status.Processing);
        kafkaTemplate.send("task-updates", event);
        Thread.sleep(3000);
        verify(emailService, times(1)).sendTextEmail(any(SingleReceiverRequest.class));
    }
}