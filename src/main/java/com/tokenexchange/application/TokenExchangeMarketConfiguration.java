package com.tokenexchange.application;

import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenExchangeMarketConfiguration {

    @Bean
    public TokenExchangeMarketPriceStorage marketPriceStorage(StatefulRedisConnection<String, String> connection) {
        return new TokenExchangeMarketPriceStorage(connection);
    }
}
