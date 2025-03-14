package app.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfiguration {

    private final Environment environment;

    @Bean
    public List<NewTopic> createTopics() {
        List<NewTopic> topics = new ArrayList<>();
        String[] topicNames = environment.getProperty("kafka.producer.topics", String[].class);

        if (topicNames != null) {
            for (String topicName : topicNames) {
                int partitions = Optional.ofNullable(environment.getProperty("kafka.producer.topics[" + topicName + "].partitions", Integer.class)).orElse(1);
                int replicas = Optional.ofNullable(environment.getProperty("kafka.producer.topics[" + topicName + "].replicas", Integer.class)).orElse(1);
                String minInsyncReplicas = Optional.ofNullable(environment.getProperty("kafka.producer.topics[" + topicName + "].min-insync-replicas")).orElse("1");

                NewTopic topic = TopicBuilder
                        .name(topicName)
                        .partitions(partitions)
                        .replicas(replicas)
                        .configs(Map.of(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInsyncReplicas))
                        .build();

                topics.add(topic);
            }
        }
        return topics;
    }

}
