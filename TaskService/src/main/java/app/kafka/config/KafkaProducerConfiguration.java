package app.kafka.config;

import app.event.TaskUpdatedStatusEvent;
import app.kafka.KafkaClientProducer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@ConfigurationProperties(prefix = "spring.kafka.producer")
@Getter
@Setter
public class KafkaProducerConfiguration {

    private String bootstrapServers;
    private String keySerializer;
    private String valueSerializer;
    private boolean idempotence;
    private String acks;
    private boolean enable = true; // Значение по умолчанию

    private Map<String, Object> producerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, idempotence);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        return config;
    }

    @Bean
    public ProducerFactory<String, TaskUpdatedStatusEvent> producerTaskFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean("taskUpdatedStatusEvent")
    public KafkaTemplate<String, TaskUpdatedStatusEvent> kafkaTemplate(ProducerFactory<String, TaskUpdatedStatusEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.kafka.producer.enable", havingValue = "true", matchIfMissing = true)
    public KafkaClientProducer clientProducer(KafkaTemplate<String, TaskUpdatedStatusEvent> kafkaTemplate) {
        return new KafkaClientProducer(kafkaTemplate);
    }
}
