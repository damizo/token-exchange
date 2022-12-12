package com.tokenexchange.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.StringJoiner;

public record MarketPriceUpdate(Token home, Token foreign, BigDecimal price) {

    @JsonCreator
    public MarketPriceUpdate(
            @JsonProperty("home") Token home,
            @JsonProperty("foreign") Token foreign,
            @JsonProperty("price") BigDecimal price) {
        this.home = home;
        this.foreign = foreign;
        this.price = price;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MarketPriceUpdate.class.getSimpleName() + "[", "]")
                .add("home=" + home)
                .add("foreign=" + foreign)
                .add("price=" + price)
                .toString();
    }


}
