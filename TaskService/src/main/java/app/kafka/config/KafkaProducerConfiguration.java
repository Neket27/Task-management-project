package app.kafka.config;

import app.event.TaskUpdatedStatusEvent;
import app.kafka.KafkaClientProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerConfiguration {

    private final Environment environment;

    private Map<String, Object> producerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.producer.bootstrap-servers"));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.producer.key-serializer"));
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.producer.value-serializer"));
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, environment.getProperty("spring.kafka.producer.idempotence"));
        config.put(ProducerConfig.ACKS_CONFIG, environment.getProperty("spring.kafka.producer.acks"));
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
