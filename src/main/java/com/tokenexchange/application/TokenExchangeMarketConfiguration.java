package com.tokenexchange.application;

import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenExchangeMarketConfiguration {

    @Bean
    public TokenExchangeMarketStorage marketPriceStorage(StatefulRedisConnection<String, String> connection) {
        return new TokenExchangeMarketStorage(connection);
    }

    @Bean
    public TokenExchangeConverter tokenExchangeNodeStorage(TokenExchangeMarketStorage marketPriceStorage){
        return new TokenExchangeConverter(
                marketPriceStorage
        );
    }
}
