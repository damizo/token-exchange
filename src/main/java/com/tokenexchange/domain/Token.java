package com.tokenexchange.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.StringJoiner;

public class Token {

    private final String value;

    private Token(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Token fromValue(String value) {
        return new Token(value);
    }

    @JsonValue
    public String value() {
        return value.toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token token)) return false;
        return Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value();
    }
}
