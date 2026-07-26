package com.swisspost.cryptowallet.client;

import com.swisspost.cryptowallet.dto.response.PriceSymbolResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class CoinCapClient {

    @Value("${coincap.api.baseUrl}")
    private String baseUrl;

    @Value("${coincap.api.key}")
    private String apiKey;

    private final RestClient restClient;

    private static final Logger log = LoggerFactory.getLogger(CoinCapClient.class);

    public CoinCapClient(RestClient.Builder restClientBuilder,
                         @Value("${coincap.api.baseUrl}") String baseUrl,
                         @Value("${coincap.api.key}") String apiKey) {
        log.info("Initializing CoinCap client...");
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        log.info("CoinCap client initialized");
    }


    public BigDecimal getAssetPriceBySymbol(String symbol){

        PriceSymbolResponse response = restClient.get()
                .uri("/price/bysymbol/{symbol}", symbol)
                .retrieve()
                .body(PriceSymbolResponse.class);

        if (response == null || response.getData() == null || response.getData().isEmpty()
                || response.getData().get(0) == null) {
            return null;
        }

        BigDecimal price = new BigDecimal(response.getData().get(0));
        log.info("Asset {} with price {}", symbol, price);

        return price;
    }


}
