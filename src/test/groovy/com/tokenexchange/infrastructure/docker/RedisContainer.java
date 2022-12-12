package com.tokenexchange.infrastructure.docker;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisContainer extends GenericContainer<RedisContainer> {

    public RedisContainer(final String version) {
        super(DockerImageName.parse(
                        String.format("redis:%s", version)
                )
        );
    }
}
