package com.tokenexchange.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.StringJoiner;

import static com.tokenexchange.infrastructure.shared.MathConstants.ROUNDING_MODE;
import static com.tokenexchange.infrastructure.shared.MathConstants.SCALE;

public record Market(Token home, Token foreign, Price price) {

    public Price multiplyBy(BigDecimal price) {
        return new Price(this.price.value().multiply(price, new MathContext(SCALE, ROUNDING_MODE)));
    }

    public String key(){
        return new StringJoiner("/")
                .add(home.value())
                .add(foreign.value())
                .toString();
    }


}
