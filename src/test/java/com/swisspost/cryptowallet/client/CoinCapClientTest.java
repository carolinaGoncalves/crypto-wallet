package com.swisspost.cryptowallet.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CoinCapClientTest {

    private static final String BASE_URL = "https://rest.coincap.io/v3";
    private static final String API_KEY = "test-api-key";
    public static final String BODY = """
            {
               "timestamp": 1785049562858,
               "data": [
                 "64411.730000000003201421"
               ]
             }
            """;
    public static final String INVALID_SYMBOL_BODY = """
            {
                "timestamp": 1785050406685,
                "data": [
                  null
                ]
              }
            """;
    public static final String BTC = "BTC";
    public static final String INVALID_SYMBOL = "INVALID";

    private MockRestServiceServer mockServer;
    private CoinCapClient coinCapClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        coinCapClient = new CoinCapClient(builder, BASE_URL, API_KEY);
    }


    @Test
    void givenSymbol_whenGetAssetPriceBySymbol_thenReturnPrice() {
        mockServer.expect(requestTo(BASE_URL + "/price/bysymbol/" + BTC))
                .andExpect(method(GET))
                .andRespond(withSuccess(BODY, MediaType.APPLICATION_JSON));

        BigDecimal price = coinCapClient.getAssetPriceBySymbol(BTC);

        assertThat(price).isNotNull();
        assertThat(price).isEqualByComparingTo(new BigDecimal("64411.730000000003201421"));
    }

    @Test
    void givenInvalidSymbol_whenGetAssetPriceBySymbol_thenReturnNullPrice() {
        mockServer.expect(requestTo(BASE_URL + "/price/bysymbol/" + INVALID_SYMBOL))
                .andExpect(method(GET))
                .andRespond(withSuccess(INVALID_SYMBOL_BODY, MediaType.APPLICATION_JSON));

        BigDecimal price = coinCapClient.getAssetPriceBySymbol(INVALID_SYMBOL);

        assertThat(price).isNull();
    }
}