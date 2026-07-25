package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.query.SymbolQuantityAsset;
import com.swisspost.cryptowallet.dto.response.WalletValueResponse;
import com.swisspost.cryptowallet.entity.PriceHistory;
import com.swisspost.cryptowallet.entity.User;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.entity.WalletAsset;
import com.swisspost.cryptowallet.exception.InvalidDateException;
import com.swisspost.cryptowallet.exception.UserNotFoundException;
import com.swisspost.cryptowallet.exception.WalletNotFoundException;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.swisspost.cryptowallet.service.WalletServiceTest.BTC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletValuationServiceTest {
    private static final String USERNAME = "username";

    @Mock
    private WalletAssetRepository walletAssetRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletValuationService walletValuationService;

    private User user;
    private Wallet wallet;
    private final LocalDate date = LocalDate.now();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername(USERNAME);
        wallet = new Wallet(user);
    }

    @Test
    void givenExistingUserWithWallet_whenGettingWalletValuation_thenValuationIsReturned() {
        wallet.setAssets(new ArrayList<>());

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(wallet));

        WalletValueResponse actualResponse = walletValuationService.getWalletValueByUsername(USERNAME, date);

        assertNotNull(actualResponse);
        assertEquals(new BigDecimal("0.0"), actualResponse.getCurrentValue());
        verify(priceHistoryRepository, never()).findLatestPricesByDate(any(), any());
        verify(walletAssetRepository, never()).sumQuantityGroupedBySymbolAndByPurchaseDate(any(), any());
    }

    @Test
    void givenInvalidUser_whenGettingWalletValue_thenUserNotFoundExceptionIsThrown() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> walletValuationService.getWalletValueByUsername(USERNAME, date));

        verify(walletRepository, never()).findByUserUsername(USERNAME);
    }

    @Test
    void givenInvalidWallet_whenGettingWalletValue_thenWalletNotFoundExceptionIsThrown() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletValuationService.getWalletValueByUsername(USERNAME, date));
    }

    @Test
    void givenValidUserAndWallet_whenGettingWalletValueForFutureDate_thenInvalidDateExceptionIsThrown() {
        LocalDate futureDate = LocalDate.now(ZoneOffset.UTC).plusDays(1);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertThrows(InvalidDateException.class,
                () -> walletValuationService.getWalletValueByUsername(USERNAME, futureDate));

        verify(walletRepository, never()).findByUserUsername(USERNAME);
    }

    @Test
    void givenWalletWithTwoAssetsButOnePurchasedAfterGivenDate_whenGettingWalletValueForDate_thenOnlyAssetBeforeDateIsIncluded() {
        UUID walletId = UUID.randomUUID();
        wallet.setId(walletId);
        wallet.setAssets(new ArrayList<>(List.of(new WalletAsset(), new WalletAsset())));

        LocalDate date = LocalDate.now();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(wallet));
        when(walletAssetRepository.sumQuantityGroupedBySymbolAndByPurchaseDate(eq(walletId), any()))
                .thenReturn(List.of(new SymbolQuantityAsset(BTC, new BigDecimal("2"))));

        PriceHistory btcPrice = new PriceHistory();
        btcPrice.setSymbol(BTC);
        btcPrice.setPrice(new BigDecimal("600"));

        when(priceHistoryRepository.findLatestPricesByDate(anyList(), any()))
                .thenReturn(List.of(btcPrice));

        WalletValueResponse actualResponse = walletValuationService.getWalletValueByUsername(USERNAME, date);

        assertNotNull(actualResponse);
        assertEquals(0, new BigDecimal("1200").compareTo(actualResponse.getCurrentValue()));
    }


}