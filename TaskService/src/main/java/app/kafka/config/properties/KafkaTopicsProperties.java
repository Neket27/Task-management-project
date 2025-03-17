package app.kafka.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "kafka.producer")
public class KafkaTopicsProperties {
    private List<String> topics;
    private Map<String, TopicConfig> topicsConfig;

    @Getter
    @Setter
    public static class TopicConfig {
        private Integer partitions = 1;
        private Integer replicas = 1;
        private String minInsyncReplicas = "1";
    }
}
