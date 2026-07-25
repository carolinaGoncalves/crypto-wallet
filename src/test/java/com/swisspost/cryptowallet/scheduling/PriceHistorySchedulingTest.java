package com.swisspost.cryptowallet.scheduling;

import com.swisspost.cryptowallet.repository.WalletAssetRepository;
import com.swisspost.cryptowallet.service.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceHistorySchedulingTest {

    private PriceHistoryScheduling priceHistoryScheduling;

    @Mock
    private PriceService priceService;

    @Mock
    private WalletAssetRepository walletAssetRepository;

    @BeforeEach
    void setUp() {
        priceHistoryScheduling = new PriceHistoryScheduling(priceService, walletAssetRepository, 3);
    }

    @Test
    public void givenEmptyDistinctSymbols_whenInsertPriceAssets_thenNoGetAndInsertPricesCalls(){
        when(walletAssetRepository.findDistinctSymbols()).thenReturn(List.of());

        priceHistoryScheduling.insertPriceAssets();

        verify(priceService, never()).getAndInsertPrices(any());
    }

    @Test
    public void givenDistinctSymbols_whenInsertPriceAssets_thenNoGetAndInsertPricesCalls(){
        when(walletAssetRepository.findDistinctSymbols()).thenReturn(List.of("ETB","MASK","ACE", "ING"));

        priceHistoryScheduling.insertPriceAssets();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(priceService, times(1)).getAndInsertPrices("ETB");
            verify(priceService, times(1)).getAndInsertPrices("MASK");
            verify(priceService, times(1)).getAndInsertPrices("ACE");
            verify(priceService, times(1)).getAndInsertPrices("ING");
        });
    }

    @Test
    void givenOneNullSymbol_whenInsertPriceAssets_thenOtherPricesInserted() {
        when(walletAssetRepository.findDistinctSymbols()).thenReturn(List.of("BTC", "TBD", "ETH"));

        priceHistoryScheduling.insertPriceAssets();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(priceService, times(1)).getAndInsertPrices("BTC");
            verify(priceService, times(1)).getAndInsertPrices("TBD");
            verify(priceService, times(1)).getAndInsertPrices("ETH");
        });
    }

    @Test
    void givenSymbolThrowsException_whenInsertPriceAssets_thenExceptionIsCaughtAndHandled() {
        when(walletAssetRepository.findDistinctSymbols()).thenReturn(List.of("BTC"));

        doThrow(new RuntimeException("CoinCap unavailable"))
                .when(priceService).getAndInsertPrices("BTC");

        assertThatCode(() -> priceHistoryScheduling.insertPriceAssets())
                .doesNotThrowAnyException();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                verify(priceService, times(1)).getAndInsertPrices("BTC")
        );
    }
}