package com.tokenexchange.application;

import com.tokenexchange.infrastructure.shared.ObjectMapperWrapper;
import com.tokenexchange.domain.MarketPriceUpdate;
import com.tokenexchange.domain.Price;
import com.tokenexchange.infrastructure.shared.Topics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
public class TokenExchangeMarketUpdateListener {

    private final Logger LOGGER = LoggerFactory.getLogger(TokenExchangeMarketUpdateListener.class);

    private final TokenExchangeMarketPriceStorage tokenPriceStorage;
    private final ObjectMapperWrapper objectMapperWrapper;

    @Autowired
    public TokenExchangeMarketUpdateListener(final TokenExchangeMarketPriceStorage tokenPriceStorage,
                                             final ObjectMapperWrapper objectMapperWrapper) {
        this.tokenPriceStorage = tokenPriceStorage;
        this.objectMapperWrapper = objectMapperWrapper;
    }

    @KafkaListener(groupId = "token-exchange", topics = Topics.MARKET_PRICE_UPDATES)
    void receive(@Payload ConsumerRecord<String, String> event) {
        LOGGER.info("Event update market price received: {}", event.value());
        MarketPriceUpdate marketPriceUpdate = objectMapperWrapper.fromJson(event.value(), MarketPriceUpdate.class);
        tokenPriceStorage.updatePrice(marketPriceUpdate.home(), marketPriceUpdate.foreign(), new Price(marketPriceUpdate.price()));
    }
}
