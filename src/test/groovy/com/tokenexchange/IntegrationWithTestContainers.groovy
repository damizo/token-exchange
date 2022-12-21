package com.tokenexchange

import com.tokenexchange.infrastructure.docker.RedisContainer
import com.tokenexchange.infrastructure.shared.Topics
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
class IntegrationWithTestContainers extends Specification {

    protected MockMvc mockMvc;

    @Shared
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
            .withExposedPorts(9092, 9093)


    @Shared
    public static final RedisContainer REDIS = new RedisContainer("6.2.6")
            .withExposedPorts(6379)

    static
    {
        KAFKA.start()
        REDIS.start()
        createTopics()
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", { -> KAFKA.getBootstrapServers() })
        registry.add("spring.kafka.consumers.bootstrap-servers", { -> KAFKA.getBootstrapServers() })
        registry.add("spring.kafka.consumers.auto-offset-reset", { -> "earliest" })

        registry.add("bootstrap.servers", { -> KAFKA.getBootstrapServers() })

        registry.add("redis.host", { -> REDIS.getHost() })
        registry.add("redis.port", { -> REDIS.getMappedPort(6379) })

    }

    private static createTopics() {
        Map<String, String> config = new HashMap<>()
        config.put("bootstrap.servers", KAFKA.getBootstrapServers())
        AdminClient client = AdminClient.create(config)
        client.createTopics(Collections.singletonList(
                new NewTopic(Topics.MARKET_PRICE_UPDATES, 1, (short) 1)
        ))
    }
}
