package app.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "kafka.producer")
@Getter
@Setter
public class KafkaTopicsConfiguration {

    private Map<String, KafkaTopicProperties> topics = new HashMap<>();

    @Bean
    public List<NewTopic> createTopics() {
        List<NewTopic> topicList = new ArrayList<>();

        for (Map.Entry<String, KafkaTopicProperties> entry : topics.entrySet()) {
            String topicName = entry.getKey();
            KafkaTopicProperties props = entry.getValue();

            NewTopic topic = TopicBuilder
                    .name(topicName)
                    .partitions(props.getPartitions())
                    .replicas(props.getReplicas())
                    .configs(Map.of(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(props.getMinInsyncReplicas())))
                    .build();

            topicList.add(topic);
        }

        return topicList;
    }

    @Getter
    @Setter
    public static class KafkaTopicProperties {
        private int partitions = 1;
        private int replicas = 1;
        private int minInsyncReplicas = 1;
    }
}

