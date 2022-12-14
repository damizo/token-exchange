package com.tokenexchange.application;

import com.tokenexchange.domain.Market;
import com.tokenexchange.domain.Price;
import com.tokenexchange.domain.Token;
import com.tokenexchange.infrastructure.exception.ErrorType;
import com.tokenexchange.infrastructure.exception.ParameterizedException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tokenexchange.infrastructure.shared.MathConstants.ROUNDING_MODE;
import static com.tokenexchange.infrastructure.shared.MathConstants.SCALE;

public class TokenExchangeMarketStorage {

    private final RedisCommands<String, String> commands;
    private static final String PREFIX_KEY = "~markets";
    private static final String PREFIX_TOKENS_KEY = "~tokens";


    public TokenExchangeMarketStorage(StatefulRedisConnection<String, String> connection) {
        this.commands = connection.sync();
    }

    public void updatePrice(Token home, Token foreign, Price price) {
        if (findMarketByTokenPair(foreign, home).isPresent()) {
            store(foreign, home, new Price(BigDecimal.ONE.divide(price.value(), SCALE, ROUNDING_MODE)));
        } else {
            store(home, foreign, price);
        }
    }

    private void store(Token home, Token foreign, Price price) {
        String key = resolveKey(home, foreign);
        this.commands.hset(PREFIX_KEY, key, price.toString());

        Set<String> tokens = this.commands.smembers(PREFIX_TOKENS_KEY);
        if (!tokens.contains(home.value())) {
            this.commands.sadd(PREFIX_TOKENS_KEY, home.value());
        }
        if (!tokens.contains(foreign.value())) {
            this.commands.sadd(PREFIX_TOKENS_KEY, foreign.value());
        }
    }

    public Optional<Market> findMarketByTokenPair(Token home, Token foreign) {
        return Optional.ofNullable(this.commands.hget(PREFIX_KEY, resolveKey(home, foreign)))
                .map(value -> new Market(home, foreign, new Price(value)));
    }

    private String resolveKey(Token home, Token foreign) {
        return new StringJoiner("/")
                .add(home.value())
                .add(foreign.value())
                .toString();
    }

    public void validateTokens(Token from, Token to) {
        Set<String> tokens = this.commands.smembers(PREFIX_TOKENS_KEY);
        if (!tokens.contains(from.value())) {
            throw new ParameterizedException(ErrorType.TOKEN_NOT_SUPPORTED, "token", from);
        }
        if (!tokens.contains(to.value())) {
            throw new ParameterizedException(ErrorType.TOKEN_NOT_SUPPORTED, "token", to);
        }
    }

    public List<String> getTokens() {
        return this.commands.smembers(PREFIX_TOKENS_KEY).stream()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public Map<String, Market> findAll() {
        return this.commands.hgetall(PREFIX_KEY)
                .entrySet()
                .stream()
                .map(value -> {
                    Token home = resolveHomeToken(value);
                    Token foreign = resolveForeignToken(value);
                    Price price = resolvePrice(value);
                    return new Market(
                            home,
                            foreign,
                            price
                    );
                })
                .collect(Collectors.toMap(Market::key,
                        Function.identity()));
    }

    private Price resolvePrice(Map.Entry<String, String> value) {
        if (Fields.PRICE.equals(value.getKey())) {
            return new Price(value.getValue());
        }
        throw new ParameterizedException(ErrorType.FIELD_NOT_RECOGNIZED, "field", value.getKey());
    }

    private Token resolveForeignToken(Map.Entry<String, String> value) {
        if (Fields.HOME.equals(value.getKey())) {
            return Token.fromValue(value.getValue());
        }
        throw new ParameterizedException(ErrorType.FIELD_NOT_RECOGNIZED, "field", value.getKey());
    }

    private Token resolveHomeToken(Map.Entry<String, String> value) {
        if (Fields.FOREIGN.equals(value.getKey())) {
            return Token.fromValue(value.getValue());
        }
        throw new ParameterizedException(ErrorType.FIELD_NOT_RECOGNIZED, "field", value.getKey());
    }


    private class Fields {
        private static final String HOME = "home";
        private static final String FOREIGN = "foreign";
        private static final String PRICE = "price";
    }
}
