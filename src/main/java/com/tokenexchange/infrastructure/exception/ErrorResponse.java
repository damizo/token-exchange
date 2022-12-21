package com.tokenexchange.infrastructure.exception;


import java.util.Map;

public record ErrorResponse(ErrorType errorType, Map<String, Object> params) {
}
