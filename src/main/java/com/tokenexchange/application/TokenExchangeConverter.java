package com.tokenexchange.application;

import com.tokenexchange.domain.Market;
import com.tokenexchange.domain.Node;
import com.tokenexchange.domain.Price;
import com.tokenexchange.domain.Token;
import com.tokenexchange.infrastructure.exception.ErrorType;
import com.tokenexchange.infrastructure.exception.ParameterizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.tokenexchange.infrastructure.shared.MathConstants.MATH_CONTEXT;

@Component
public class TokenExchangeConverter {

    private ConcurrentHashMap<Token, Node<Token>> nodesByToken = new ConcurrentHashMap<>();
    private TokenExchangeMarketStorage tokenExchangeMarketStorage;

    @Autowired
    public TokenExchangeConverter(TokenExchangeMarketStorage tokenExchangeMarketStorage) {
        this.tokenExchangeMarketStorage = tokenExchangeMarketStorage;
    }

    public Optional<Node<Token>> getNode(Token token) {
        return Optional.ofNullable(nodesByToken.get(token));
    }

    public Optional<Price> convert(BigDecimal fromAmount, Token home, Token foreign) {
        Node<Token> homeToken = getToken(home);
        Node<Token> foreignToken = getToken(foreign);
        return resolveMarket(fromAmount, home, foreign).or(
                () -> traverseAndCalculatePrice(fromAmount, homeToken, foreignToken)
        );
    }

    private Optional<Price> resolveMarket(BigDecimal fromAmount, Token home, Token foreign) {
        Optional<Market> homeToForeignMarket = tokenExchangeMarketStorage.findMarketByTokenPair(home, foreign);
        if (homeToForeignMarket.isPresent()) {
            return Optional.of(homeToForeignMarket.get().multiplyBy(fromAmount));
        } else {
            Optional<Market> foreignToHomeMarket = tokenExchangeMarketStorage.findMarketByTokenPair(foreign, home);
            if (foreignToHomeMarket.isPresent()) {
                return Optional.of(new Price(
                        fromAmount.divide(foreignToHomeMarket.get().price().value(), MATH_CONTEXT)
                ));
            }
        }
        return Optional.empty();
    }

    private Optional<Price> traverseAndCalculatePrice(BigDecimal fromAmount, Node<Token> homeToken, Node<Token> foreignToken) {
        Queue<Node<Token>> queue = new ArrayDeque<>(homeToken.getNeighbours());
        Node<Token> currentNode = null;
        List<Node<Token>> alreadyVisited = new ArrayList<>();
        alreadyVisited.add(homeToken);
        Price price = new Price(fromAmount);

        while (!queue.isEmpty()) {
            currentNode = queue.remove();

            Optional<Market> marketHomeToForeign = tokenExchangeMarketStorage.findMarketByTokenPair(homeToken.getValue(), currentNode.getValue());
            if (marketHomeToForeign.isPresent()) {
                price = marketHomeToForeign.get().multiplyBy(price.value());
            } else {
                Optional<Market> foreignToHome = tokenExchangeMarketStorage.findMarketByTokenPair(currentNode.getValue(), homeToken.getValue());
                if (foreignToHome.isPresent()) {
                    price = Price.of(price.value().divide(foreignToHome.get().price().value(), MATH_CONTEXT));
                }
            }

            if (currentNode.equals(foreignToken)) {
                return Optional.of(price);
            } else {
                alreadyVisited.add(currentNode);
                queue.addAll(currentNode.getNeighbours());
                queue.removeAll(alreadyVisited);
            }
            homeToken = currentNode;
        }
        return Optional.empty();
    }

    private Node<Token> getToken(Token home) {
        return Optional.ofNullable(this.nodesByToken.get(home))
                .orElseThrow(() -> new ParameterizedException(ErrorType.TOKEN_NOT_SUPPORTED, "token", home.value()));
    }

    public void addNode(Token home, Token foreign) {
        Node<Token> homeToken = getNode(home).orElseGet(() -> {
            Node<Token> value = new Node<>(home);
            nodesByToken.put(home, value);
            return value;
        });
        Node<Token> foreignToken = getNode(foreign).orElseGet(() -> {
            Node<Token> value = new Node<>(foreign);
            nodesByToken.put(foreign, value);
            return value;
        });
        homeToken.connect(foreignToken);
    }
}
