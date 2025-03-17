package app.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaClientProducer<V> {

    private final KafkaTemplate<String, V> kafkaTemplate;

    public void sendTo(String topic, V message) {
        String idMessage = UUID.randomUUID().toString();
        CompletableFuture<SendResult<String, V>> future = kafkaTemplate
                .send(topic, idMessage, message);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Error while sending event: {}", exception.getMessage());
            } else {
                log.info("Successfully sent message to topic: {}", result.getProducerRecord().topic());
                log.info("Partition: {}", result.getRecordMetadata().partition());
                log.info("Offset: {}", result.getRecordMetadata().offset());
                log.info("Key: {}", result.getProducerRecord().key());
                log.info("Value: {}", result.getProducerRecord().value());
                log.info("Timestamp: {}", new Date(result.getRecordMetadata().timestamp()));
            }
        });

        log.info("Message sent to topic: {}, idMessage: {}", topic, idMessage);
    }

}
