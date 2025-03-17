package app.kafka.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka.producer")
public class KafkaProducerProperties {

    private String bootstrapServers;
    private String keySerializer;
    private String valueSerializer;
    private boolean idempotence;
    private String acks;
    private boolean enable = true;

}
