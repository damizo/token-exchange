package com.tokenexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableRedisRepositories
@SpringBootApplication
public class TokenExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenExchangeApplication.class, args);
    }

}
