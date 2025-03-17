package app.kafka.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public class KafkaConsumerProperties {

    private String bootstrapServers;
    private String keyDeserializer;
    private String groupId;
    private String autoOffsetReset;
    private boolean enableAutoCommit;
    private boolean batchListener = false;
    private int concurrency = 1;
    private Properties properties;

    @Getter
    @Setter
    public static class Properties {
        private String springJsonTrustedPackages = "*";
        private boolean micrometerEnabled = false;
        private ContainerProperties.AckMode ackMode = ContainerProperties.AckMode.BATCH;
        private int pollTimeout = 1000;
    }

}
