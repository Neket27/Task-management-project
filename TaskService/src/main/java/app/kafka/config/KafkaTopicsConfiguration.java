package app.kafka.config;

import app.kafka.config.properties.KafkaTopicsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfiguration {

    private final KafkaTopicsProperties prop;

    @Bean
    public List<NewTopic> createTopics() {
        List<NewTopic> topics = new ArrayList<>();
        List<String> topicNames = prop.getTopics();

        if (topicNames != null) {
            for (String topicName : topicNames) {
                KafkaTopicsProperties.TopicConfig config = prop.getTopicsConfig().get(topicName);

                NewTopic topic = TopicBuilder
                        .name(topicName)
                        .partitions(config.getPartitions())
                        .replicas(config.getPartitions())
                        .configs(Map.of(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, config.getMinInsyncReplicas()))
                        .build();

                topics.add(topic);
            }
        }
        return topics;
    }

}
