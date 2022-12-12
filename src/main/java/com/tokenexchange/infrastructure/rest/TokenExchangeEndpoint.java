package com.tokenexchange.infrastructure.rest;

import com.tokenexchange.application.TokenExchangeService;
import com.tokenexchange.domain.Token;
import com.tokenexchange.infrastructure.exception.ErrorType;
import com.tokenexchange.infrastructure.exception.ParameterizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/token-exchange")
public class TokenExchangeEndpoint {

    private final TokenExchangeService tokenExchangeService;

    @Autowired
    public TokenExchangeEndpoint(final TokenExchangeService tokenExchangeService) {
        this.tokenExchangeService = tokenExchangeService;
    }

    @PostMapping
    public TokenExchangeResponse exchange(@RequestBody TokenExchangeMarketRequest request) {
        return tokenExchangeService.exchange(
                Token.fromValue(request.from()),
                Token.fromValue(request.to()),
                request.fromAmount()
        );
    }

    @GetMapping
    public List<String> getTokens() {
        return tokenExchangeService.getTokens();
    }


}
