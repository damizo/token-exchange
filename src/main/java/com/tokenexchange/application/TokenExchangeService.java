package com.tokenexchange.application;

import com.tokenexchange.domain.Market;
import com.tokenexchange.domain.Price;
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
import java.util.Optional;

import static com.tokenexchange.infrastructure.shared.MathConstants.ROUNDING_MODE;
import static com.tokenexchange.infrastructure.shared.MathConstants.SCALE;

@Service
public class TokenExchangeService {

    private final Logger LOGGER = LoggerFactory.getLogger(TokenExchangeService.class);
    private final TokenExchangeMarketPriceStorage marketPriceStorage;

    @Autowired
    public TokenExchangeService(TokenExchangeMarketPriceStorage marketPriceStorage) {
        this.marketPriceStorage = marketPriceStorage;
    }

    public TokenExchangeResponse exchange(Token from, Token to, BigDecimal fromAmount) {
        marketPriceStorage.validateTokens(from, to);
        LOGGER.info("Token exchange, from: {}, to: {}, amount: {}", from, to, fromAmount);
        Optional<Market> homeToForeignPrice = marketPriceStorage.findMarketByTokenPair(from, to);
        if (homeToForeignPrice.isPresent()) {
            return new TokenExchangeResponse(
                    from,
                    to,
                    homeToForeignPrice.get().multiplyBy(fromAmount));
        }
        return marketPriceStorage.findMarketByTokenPair(to, from)
                .map(value -> reverseCalculationBasedOnForeignToken(fromAmount, value))
                .map(value -> new TokenExchangeResponse(from, to, value))
                .orElseThrow(() -> new ParameterizedException(ErrorType.MARKET_NOT_FOUND, "from", from, "to", to));

    }

    private Price reverseCalculationBasedOnForeignToken(BigDecimal fromAmount, Market market) {
        Price price = market.price();
        return new Price(
                fromAmount.divide(price.value(), SCALE, ROUNDING_MODE)
        );
    }

    public List<String> getTokens() {
        return marketPriceStorage.getTokens();
    }
}
