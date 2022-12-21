package com.tokenexchange.application;

import com.tokenexchange.domain.Token;
import com.tokenexchange.infrastructure.exception.ErrorType;
import com.tokenexchange.infrastructure.exception.ParameterizedException;
import com.tokenexchange.infrastructure.rest.TokenExchangeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TokenExchangeService {

    private final Logger LOGGER = LoggerFactory.getLogger(TokenExchangeService.class);
    private final TokenExchangeMarketStorage marketPriceStorage;
    private final TokenExchangeConverter tokenExchangeNodeStorage;

    @Autowired
    public TokenExchangeService(final TokenExchangeMarketStorage marketPriceStorage,
                                final TokenExchangeConverter tokenExchangeNodeStorage) {
        this.marketPriceStorage = marketPriceStorage;
        this.tokenExchangeNodeStorage = tokenExchangeNodeStorage;
    }

    public TokenExchangeResponse exchange(Token from, Token to, BigDecimal fromAmount) {
        marketPriceStorage.validateTokens(from, to);
        LOGGER.info("Token exchange, from: {}, to: {}, amount: {}", from, to, fromAmount);
        return tokenExchangeNodeStorage
                .convert(fromAmount, from, to)
                .map(price -> new TokenExchangeResponse(from, to, price))
                .orElseThrow(() -> new ParameterizedException(ErrorType.COULD_NOT_CONVERT_TOKENS, "home", from, "foreign", to));
    }

    public List<String> getTokens() {
        return marketPriceStorage.getTokens();
    }
}
