package com.tokenexchange.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.tokenexchange.infrastructure.shared.MathConstants;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public class Price {

    private final BigDecimal value;

    public Price(final Long value) {
        this.value = BigDecimal.valueOf(value);
    }

    public static Price of(final BigDecimal value) {
        return new Price(value);
    }

    public static Price of(final Long value) {
        return new Price(value);
    }

    public Price(final String value) {
        this.value = new BigDecimal(value);
    }

    public Price(final BigDecimal value) {
        this.value = value;
    }

    @JsonValue
    public BigDecimal value() {
        return new BigDecimal(value.toPlainString(), new MathContext(MathConstants.SCALE, MathConstants.ROUNDING_MODE));
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Price price)) return false;
        return Objects.equals(value, price.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
