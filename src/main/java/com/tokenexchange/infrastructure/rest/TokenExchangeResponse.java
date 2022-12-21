package com.tokenexchange.infrastructure.rest;

import com.tokenexchange.domain.Price;
import com.tokenexchange.domain.Token;

public record TokenExchangeResponse(Token from, Token to, Price convertedValue) {

}
