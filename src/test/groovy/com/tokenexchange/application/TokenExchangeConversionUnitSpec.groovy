package com.tokenexchange.application

import com.tokenexchange.IntegrationWithTestContainers
import com.tokenexchange.TokenExchangeApplication
import com.tokenexchange.domain.MarketPriceUpdate
import com.tokenexchange.domain.Price
import com.tokenexchange.domain.Token
import com.tokenexchange.infrastructure.shared.ObjectMapperWrapper
import com.tokenexchange.infrastructure.shared.Topics
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.TestPropertySource
import org.testcontainers.shaded.org.awaitility.Awaitility
import spock.lang.Shared

import java.util.concurrent.TimeUnit


@SpringBootTest(classes = [
        TokenExchangeApplication.class
])
@TestPropertySource(locations = "classpath:application.properties")
class TokenExchangeConversionUnitSpec extends IntegrationWithTestContainers {

    @Shared
    private final Token ETH = Token.fromValue("ETH")
    @Shared
    private final Token USDT = Token.fromValue("USDT")
    @Shared
    private final Token BTC = Token.fromValue("BTC")
    @Shared
    private final Token DOT = Token.fromValue("DOT")
    @Shared
    private final Token ADA = Token.fromValue("ADA")

    @Autowired
    private TokenExchangeConverter tokenExchangeConverter

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate

    @Autowired
    private ObjectMapperWrapper objectMapperWrapper

    def setup() {
        publishUpdatePriceEvents()
    }


    def 'should convert tokens'() {
        given:
        tokenExchangeConverter.addNode(ETH, USDT)
        tokenExchangeConverter.addNode(USDT, BTC)
        tokenExchangeConverter.addNode(BTC, DOT)
        tokenExchangeConverter.addNode(ADA, DOT)

        expect:
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            tokenExchangeConverter.convert(value, homeToken, foreignToken).get().value() == expectedPrice
        }

        where:
        value <<        [Price.of(1254).value(),      Price.of(200).value(),       Price.of(1).value(),          Price.of(1000).value()    ]
        homeToken <<    [ETH,                         ETH,                         ETH,                          ADA                       ]
        foreignToken << [USDT,                        BTC,                         ADA,                          USDT                      ]
        expectedPrice <<[Price.of(1572516).value(),   Price.of(14.77729).value(),  Price.of(3979.983).value(),   Price.of(315.0784).value()]
    }

    def publishUpdatePriceEvent(String home, String foreign, Price price) {
        def message = objectMapperWrapper.toJson(new MarketPriceUpdate(
                Token.fromValue(home),
                Token.fromValue(foreign),
                price.value()
        ))
        kafkaTemplate.send(new ProducerRecord<String, String>(Topics.MARKET_PRICE_UPDATES, message))
    }

    private void publishUpdatePriceEvents() {
        publishUpdatePriceEvent("BTC", "USDT", new Price(16972))
        publishUpdatePriceEvent("BTC", "DOT", new Price(3107))
        publishUpdatePriceEvent("ETH", "USDT", new Price(1254))
        publishUpdatePriceEvent("ADA", "DOT", new Price(0.05768))
    }

}
