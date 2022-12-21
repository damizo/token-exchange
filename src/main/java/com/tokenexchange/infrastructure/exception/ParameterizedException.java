package com.tokenexchange.infrastructure.exception;


import java.util.HashMap;

public class ParameterizedException extends RuntimeException {

    private ErrorType code;
    private HashMap<String, Object> params = new HashMap<>();

    public ParameterizedException(ErrorType code, String firstKey,
                                  Object firstValue,
                                  String secondKey,
                                  Object secondValue) {
        this.code = code;
        params.put(firstKey, firstValue);
        params.put(secondKey, secondValue);
    }

    public ParameterizedException(ErrorType code, String firstKey,
                                  Object firstValue) {
        this.code = code;
        params.put(firstKey, firstValue);
    }

    public ParameterizedException(ErrorType code) {
        this.code = code;
    }

    public ErrorType getCode() {
        return code;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return code.name() + ", params: " + params.toString();
    }
}
