package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.query.SymbolQuantityAsset;
import com.swisspost.cryptowallet.dto.query.SymbolTotalInvestment;
import com.swisspost.cryptowallet.dto.response.WalletPerformanceResponse;
import com.swisspost.cryptowallet.entity.PriceHistory;
import com.swisspost.cryptowallet.entity.User;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.repository.PriceHistoryRepository;
import com.swisspost.cryptowallet.repository.UserRepository;
import com.swisspost.cryptowallet.repository.WalletAssetRepository;
import com.swisspost.cryptowallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletPerformanceServiceTest {

    private static final String USERNAME = "username";
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private WalletAssetRepository walletAssetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletPerformanceService walletPerformanceService;

    private UUID walletId;

    @BeforeEach
    void setUp() {
        User user = new User(USERNAME, "username", LocalDateTime.now());
        Wallet wallet = new Wallet(user);
        walletId = UUID.randomUUID();
        wallet.setId(walletId);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(wallet));
    }

    @Test
    void givenValidUserAndWallet_whenGetWalletPerformancesByUsername_thenReturnsWalletPerformanceResponse() {
        LocalDateTime expectedFilterDate = LocalDate.now(ZoneOffset.UTC).atTime(LocalTime.MAX);

        SymbolTotalInvestment btcInvestment = new SymbolTotalInvestment(BTC, BigDecimal.valueOf(50000));
        SymbolTotalInvestment ethInvestment = new SymbolTotalInvestment(ETH, BigDecimal.valueOf(20000));

        when(walletAssetRepository.sumTotalInvestmentGroupedBySymbol(walletId))
                .thenReturn(List.of(btcInvestment, ethInvestment));

        SymbolQuantityAsset btcQuantity = new SymbolQuantityAsset(BTC, BigDecimal.valueOf(2));
        SymbolQuantityAsset ethQuantity = new SymbolQuantityAsset(ETH, BigDecimal.valueOf(10));

        when(walletAssetRepository.sumQuantityGroupedBySymbolAndByPurchaseDate(walletId, expectedFilterDate))
                .thenReturn(List.of(btcQuantity, ethQuantity));

        PriceHistory btcPrice = new PriceHistory();
        btcPrice.setSymbol(BTC);
        btcPrice.setPrice(BigDecimal.valueOf(30000));

        PriceHistory ethPrice = new PriceHistory();
        ethPrice.setSymbol(ETH);
        ethPrice.setPrice(BigDecimal.valueOf(1500));

        when(priceHistoryRepository.findLatestPricesByDate(List.of(BTC, ETH), expectedFilterDate))
                .thenReturn(List.of(btcPrice, ethPrice));

        WalletPerformanceResponse response = walletPerformanceService.getWalletPerformancesByUsername(USERNAME);

        assertEquals(2, response.getAssets().size());
        assertEquals(BTC, response.getBestPerforming().getSymbol());
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(response.getBestPerforming().getPerformancePercentage()));
        assertEquals(ETH, response.getWorstPerforming().getSymbol());
        assertEquals(0, BigDecimal.valueOf(-25.00).compareTo(response.getWorstPerforming().getPerformancePercentage()));
    }

    @Test
    void givenWalletAssetWithoutPriceAvailable_whenGetWalletPerformancesByUsername_thenExcludesAssetWithoutPrice() {
        LocalDateTime expectedFilterDate = LocalDate.now(ZoneOffset.UTC).atTime(LocalTime.MAX);

        SymbolTotalInvestment ethInvestment = new SymbolTotalInvestment(ETH, BigDecimal.valueOf(20000));

        when(walletAssetRepository.sumTotalInvestmentGroupedBySymbol(walletId))
                .thenReturn(List.of(ethInvestment));

        SymbolQuantityAsset ethQuantity = new SymbolQuantityAsset(ETH, BigDecimal.valueOf(10));

        when(walletAssetRepository.sumQuantityGroupedBySymbolAndByPurchaseDate(walletId, expectedFilterDate))
                .thenReturn(List.of(ethQuantity));
        when(priceHistoryRepository.findLatestPricesByDate(List.of(ETH), expectedFilterDate))
                .thenReturn(Collections.emptyList());

        WalletPerformanceResponse response = walletPerformanceService.getWalletPerformancesByUsername(USERNAME);

        assertTrue(response.getAssets().isEmpty());
        assertNull(response.getBestPerforming());
        assertNull(response.getWorstPerforming());
    }

    @Test
    void givenEmptyWallet_whenGetWalletPerformancesByUsername_thenReturnsEmptyWalletPerformanceResponse() {
        LocalDateTime expectedFilterDate = LocalDate.now(ZoneOffset.UTC).atTime(LocalTime.MAX);

        when(walletAssetRepository.sumTotalInvestmentGroupedBySymbol(walletId))
                .thenReturn(Collections.emptyList());

        when(walletAssetRepository.sumQuantityGroupedBySymbolAndByPurchaseDate(walletId, expectedFilterDate))
                .thenReturn(Collections.emptyList());

        WalletPerformanceResponse response = walletPerformanceService.getWalletPerformancesByUsername(USERNAME);

        assertTrue(response.getAssets().isEmpty());
        assertNull(response.getBestPerforming());
        assertNull(response.getWorstPerforming());
    }

    @Test
    void givenAssetPurchasedTodayDate_whenGetWalletPerformancesByUsername_thenFilterDateForTheMaxValue() {
        LocalDateTime expectedFilterDate = LocalDate.now(ZoneOffset.UTC).atTime(LocalTime.MAX);

        SymbolTotalInvestment btcInvestment = new SymbolTotalInvestment(BTC, BigDecimal.valueOf(50000));
        SymbolQuantityAsset btcQuantity = new SymbolQuantityAsset(BTC, BigDecimal.valueOf(2));

        PriceHistory btcPrice = new PriceHistory();
        btcPrice.setSymbol(BTC);
        btcPrice.setPrice(BigDecimal.valueOf(30000));

        when(walletAssetRepository.sumTotalInvestmentGroupedBySymbol(walletId))
                .thenReturn(List.of(btcInvestment));
        when(walletAssetRepository.sumQuantityGroupedBySymbolAndByPurchaseDate(walletId, expectedFilterDate))
                .thenReturn(List.of(btcQuantity));
        when(priceHistoryRepository.findLatestPricesByDate(List.of(BTC), expectedFilterDate))
                .thenReturn(List.of(btcPrice));

        walletPerformanceService.getWalletPerformancesByUsername(USERNAME);

        verify(walletAssetRepository).sumQuantityGroupedBySymbolAndByPurchaseDate(walletId, expectedFilterDate);
    }

}