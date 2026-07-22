package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.client.CoinCapClient;
import com.swisspost.cryptowallet.entity.PriceHistory;
import com.swisspost.cryptowallet.repository.PriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PriceService {

    private final CoinCapClient coinCapClient;

    private final PriceHistoryRepository priceHistoryRepository;

    private static final Logger log = LoggerFactory.getLogger(PriceService.class);

    public PriceService(CoinCapClient coinCapClient, PriceHistoryRepository priceHistoryRepository) {
        this.coinCapClient = coinCapClient;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public void getAndInsertPrices(String symbol){
        BigDecimal value = coinCapClient.getAssetPriceByName(symbol);

        if(Objects.isNull(value)){
            log.info("Price for asset {} is null. It will be ignored.", symbol);
            return;
        }

        PriceHistory record = new PriceHistory(symbol, value, LocalDateTime.now());
        priceHistoryRepository.save(record);

    }
}
