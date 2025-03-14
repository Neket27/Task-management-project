package app.kafka.config;

import app.kafka.MessageDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Configuration
@Slf4j
public class KafkaConfig {

    private final Environment environment;

    public KafkaConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.consumer.bootstrap-servers"));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.key-deserializer"));
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"));
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, MessageDeserializer.class.getName());
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, MessageDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, environment.getProperty("spring.kafka.consumer.enable-auto-commit"));

        return new DefaultKafkaConsumerFactory<>(config);
    }


    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(Optional.ofNullable(environment.getProperty("spring.kafka.consumer.batchListener", Boolean.class)).orElse(false));
        factory.setConcurrency(Optional.ofNullable(environment.getProperty("spring.kafka.consumer.concurrency", Integer.class)).orElse(1));

        ContainerProperties.AckMode mode = ContainerProperties.AckMode.valueOf(Optional.ofNullable(environment.getProperty("spring.kafka.consumer.properties.ack-mode")).orElse(ContainerProperties.AckMode.BATCH.toString()).toUpperCase());
        factory.getContainerProperties().setAckMode(mode);
        factory.getContainerProperties().setPollTimeout(Optional.ofNullable(environment.getProperty("spring.kafka.consumer.poll-timeout", Integer.class)).orElse(1000));
        factory.getContainerProperties().setMicrometerEnabled(Optional.ofNullable(environment.getProperty("spring.kafka.consumer.micrometer-enabled", Boolean.class)).orElse(false));
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        handler.setRetryListeners(((record, ex, deliveryAttempt) ->
                log.error("RetryListeners massage= {}, offset = {}, error = {}", record, ex, deliveryAttempt)
        ));
        return handler;
    }

}
