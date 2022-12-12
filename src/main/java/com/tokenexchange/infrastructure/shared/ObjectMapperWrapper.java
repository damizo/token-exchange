package com.tokenexchange.infrastructure.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokenexchange.application.TokenExchangeMarketUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperWrapper {
    private final Logger LOGGER = LoggerFactory.getLogger(TokenExchangeMarketUpdateListener.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public ObjectMapperWrapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error occurred during marshalling", e);
            return null;
        }
    }

    public <T> T fromJson(String value, Class<T> t) {
        try {
            return objectMapper.readValue(value, t);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error occurred during unmarshalling", e);
            return null;
        }
    }


}
