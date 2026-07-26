package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.client.CoinCapClient;
import com.swisspost.cryptowallet.entity.PriceHistory;
import com.swisspost.cryptowallet.repository.PriceHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    public static final String SYMBOL = "TBD";
    public static final BigDecimal PRICE = new BigDecimal("32.22");

    @InjectMocks
    private PriceService priceService;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private CoinCapClient coinCapClient;

    @Test
    public void givenPriceNotNull_whenGetAndInsertPrices_thenInsertInPriceHistory(){
        when(coinCapClient.getAssetPriceBySymbol(SYMBOL)).thenReturn(PRICE);

        priceService.getAndInsertPrices(SYMBOL);

        verify(priceHistoryRepository, times(1)).save(any());

    }

    @Test
    public void givenPriceNull_whenGetAndInsertPrices_thenNotInsertInPriceHistory(){
        when(coinCapClient.getAssetPriceBySymbol(SYMBOL)).thenReturn(null);

        priceService.getAndInsertPrices(SYMBOL);

        verify(priceHistoryRepository, never()).save(any());
    }

}