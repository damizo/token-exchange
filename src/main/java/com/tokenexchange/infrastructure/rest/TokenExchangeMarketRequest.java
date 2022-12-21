package com.tokenexchange.infrastructure.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tokenexchange.domain.Token;

import java.math.BigDecimal;
import java.util.StringJoiner;

public record TokenExchangeMarketRequest(String from, String to, BigDecimal fromAmount) {
    @JsonCreator
    public TokenExchangeMarketRequest(
            @JsonProperty("from") String from,
            @JsonProperty("to") String to,
            @JsonProperty("fromAmount") BigDecimal fromAmount) {
        this.from = from;
        this.to = to;
        this.fromAmount = fromAmount;
    }
    @Override
    public String toString() {
        return new StringJoiner(", ", TokenExchangeMarketRequest.class.getSimpleName() + "[", "]")
                .add("from=" + from)
                .add("to=" + to)
                .add("fromAmount=" + fromAmount)
                .toString();
    }
}
