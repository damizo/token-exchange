package com.tokenexchange.infrastructure.exception;

public enum ErrorType {
    MARKET_NOT_FOUND("Couldn't find corresponding pair in the market"),
    INTERNAL_ERROR("Internal error"),
    TOKEN_NOT_SUPPORTED("Token not supported"),
    PRICE_IS_ZERO_OR_NEGATIVE("Price has to be greater than 0"),
    MARKET_PAIR_NOT_COMPLETE("Home and foreign value have to be filled"),
    FIELD_NOT_RECOGNIZED("This field is not compatible with Market object"),
    COULD_NOT_CONVERT_TOKENS("For some reasons conversion failed, check logs");

    private final String value;

    ErrorType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
