package com.tokenexchange.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.util.Objects;

public class Price {

    private final BigDecimal value;

    public Price(final Long value) {
        this.value = BigDecimal.valueOf(value);
    }

    public Price(final String value) {
        this.value = new BigDecimal(value);
    }

    public Price(final BigDecimal value) {
        this.value = value;
    }

    @JsonValue
    public BigDecimal value() {
        return value;
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
