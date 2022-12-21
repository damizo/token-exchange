package com.tokenexchange.infrastructure.rest;

import com.tokenexchange.domain.MarketPriceUpdate;
import com.tokenexchange.infrastructure.shared.ObjectMapperWrapper;
import com.tokenexchange.infrastructure.shared.Topics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/publish-event")
public class KafkaEventPublisherEndpoint {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapperWrapper objectMapperWrapper;


    @Autowired
    public KafkaEventPublisherEndpoint(final KafkaTemplate<String, String> kafkaTemplate,
                                       final ObjectMapperWrapper objectMapperWrapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapperWrapper = objectMapperWrapper;
    }

    @PostMapping
    public void publish(@RequestBody MarketPriceUpdate request) {
        try {
            kafkaTemplate.send(
                    Topics.MARKET_PRICE_UPDATES,
                    objectMapperWrapper.toJson(request)
            ).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
