package com.tokenexchange.application

import com.tokenexchange.IntegrationWithTestContainers
import com.tokenexchange.TokenExchangeApplication
import com.tokenexchange.domain.MarketPriceUpdate
import com.tokenexchange.domain.Price
import com.tokenexchange.domain.Token
import com.tokenexchange.infrastructure.GlobalRestControllerAdvice
import com.tokenexchange.infrastructure.rest.TokenExchangeEndpoint
import com.tokenexchange.infrastructure.rest.TokenExchangeMarketRequest
import com.tokenexchange.infrastructure.shared.ObjectMapperWrapper
import com.tokenexchange.infrastructure.shared.Topics
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.testcontainers.shaded.org.awaitility.Awaitility

import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [
        TokenExchangeApplication.class
])
@TestPropertySource(locations = "classpath:application.properties")
class TokenExchangeConversionIntegrationSpec extends IntegrationWithTestContainers {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate

    @Autowired
    private ObjectMapperWrapper objectMapperWrapper

    @Autowired
    private TokenExchangeEndpoint tokenExchangeEndpoint

    def setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tokenExchangeEndpoint)
                .setControllerAdvice(new GlobalRestControllerAdvice())
                .alwaysDo(MockMvcResultHandlers.print())
                .build()
        publishUpdatePriceEvents()
    }

    def 'should exchange DOT to BTC without DOT value stored'() {
        when: 'try to convert 35000 DOT to BTC'
        def bitcoinToPolkadotRequest = new TokenExchangeMarketRequest(
                "DOT",
                "BTC",
                BigDecimal.valueOf(35000)
        )


        then: 'should return 11.26 DOT'
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted {
            mockMvc.perform(MockMvcRequestBuilders.post('/api/v1/token-exchange')
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapperWrapper.toJson(bitcoinToPolkadotRequest)))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.from").value("DOT"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.to").value("BTC"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.convertedValue").value("11.2648857419"))
        }
    }

    def 'should exchange USDT to BTC'() {
        when: 'try to convert 10000 USDT to BTC'
        def bitcoinToTetherRequest = new TokenExchangeMarketRequest(
                "USDT",
                "BTC",
                BigDecimal.valueOf(10000)
        )
        then: 'should return 0.58 BTC'
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted {
            mockMvc.perform(MockMvcRequestBuilders.post('/api/v1/token-exchange')
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapperWrapper.toJson(bitcoinToTetherRequest)))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.from").value("USDT"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.to").value("BTC"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.convertedValue").value("0.5892057507"))
        }
    }

    def 'should exchange home token properly when only foreign key is updated'() {
        given: 'new price event for XMR/USDT'
        publishUpdatePriceEvent("XMR", "USDT", new Price(5))

        when: 'try to convert 1511 XMR to USDT'
        def moneroToTetherRequest = new TokenExchangeMarketRequest(
                "XMR",
                "USDT",
                BigDecimal.valueOf(1511.89)
        )

        then: 'should return 7559 dollars'
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted {
            mockMvc.perform(MockMvcRequestBuilders.post('/api/v1/token-exchange')
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapperWrapper.toJson(moneroToTetherRequest)))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.from").value("XMR"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.to").value("USDT"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.convertedValue").value("7559.45"))
        }

        when: 'new price event for USDT/XMR'
        publishUpdatePriceEvent("USDT", "XMR", new Price(1241))

        then: 'should return new price for previous market'
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted {
            mockMvc.perform(MockMvcRequestBuilders.post('/api/v1/token-exchange')
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapperWrapper.toJson(moneroToTetherRequest)))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.from").value("XMR"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.to").value("USDT"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.convertedValue").value("1.218283684"))
        }

        and: 'should return new price for USDT/XMR'
        def tetherToMoneroRequest = new TokenExchangeMarketRequest(
                "USDT",
                "XMR",
                BigDecimal.valueOf(350)
        )

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            mockMvc.perform(MockMvcRequestBuilders.post('/api/v1/token-exchange')
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapperWrapper.toJson(tetherToMoneroRequest)))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.from").value("USDT"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.to").value("XMR"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$.convertedValue").value("434349.9853189705"))
        }

    }

    def 'should not exchange ADA to USDT when such pair not exists'() {
        when: 'try to convert 1400 ADA to USDT'
        def cardanoToTetherRequest = new TokenExchangeMarketRequest(
                "ADA",
                "USDT",
                BigDecimal.valueOf(1400)
        )
        def cardanoToTetherResponse = mockMvc.perform(MockMvcRequestBuilders.post('/api/v1/token-exchange')
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapperWrapper.toJson(cardanoToTetherRequest)))

        then: 'should thrown an exception about not existing market pair'
        cardanoToTetherResponse
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("\$.errorType").value("MARKET_NOT_FOUND"))
    }

    def 'should not exchange ETH to ENJ when such token not exists'() {
        when: 'try to convert ETH to ENJ'
        def ethereumToEnjRequest = new TokenExchangeMarketRequest(
                "ETH",
                "ENJ",
                BigDecimal.valueOf(124.3)
        )
        def ethereumToEnjResponse = mockMvc.perform(MockMvcRequestBuilders.post('/api/v1/token-exchange')
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapperWrapper.toJson(ethereumToEnjRequest)))

        then: 'should thrown an exception about not existing token'
        ethereumToEnjResponse
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("\$.errorType").value("TOKEN_NOT_SUPPORTED"))
    }

    def 'should return all tokens after updated price'() {
        when: 'try to get tokens'
        then: 'all available tokens should be displayed'
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            def tokensResponse = mockMvc.perform(MockMvcRequestBuilders.get('/api/v1/token-exchange'))
            tokensResponse.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(MockMvcResultMatchers.jsonPath("\$[0]").value("ADA"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$[1]").value("BTC"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$[2]").value("DOT"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$[3]").value("ETH"))
                    .andExpect(MockMvcResultMatchers.jsonPath("\$[4]").value("USDT"))
        }
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
