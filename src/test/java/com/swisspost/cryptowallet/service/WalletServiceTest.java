package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.request.WalletAssetRequest;
import com.swisspost.cryptowallet.dto.request.WalletRequest;
import com.swisspost.cryptowallet.dto.response.WalletResponse;
import com.swisspost.cryptowallet.entity.User;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.entity.WalletAsset;
import com.swisspost.cryptowallet.exception.UserNotFoundException;
import com.swisspost.cryptowallet.exception.WalletAlreadyExistsException;
import com.swisspost.cryptowallet.exception.WalletNotFoundException;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    private static final String USERNAME = "username";
    public static final String BTC = "BTC";

    @Mock
    private WalletAssetRepository walletAssetRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletService walletService;

    private User user;
    private Wallet wallet;
    private WalletRequest request;
    private WalletAssetRequest assetRequest;

    @BeforeEach
    void setUp() {
        request = new WalletRequest(USERNAME);
        user = new User();
        wallet = new Wallet(user);
        assetRequest = new WalletAssetRequest(BTC, new BigDecimal("1.5"),
                new BigDecimal("45.33"), LocalDateTime.now(ZoneOffset.UTC));

    }


    @Test
    void givenValidUserWithNoWallet_whenCreatingWallet_thenWalletIsCreated() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        WalletResponse actualResponse = walletService.create(request);

        assertThat(actualResponse).isNotNull();
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void givenUserAlreadyHasWallet_whenCreatingWallet_thenWalletAlreadyExistsExceptionIsThrown() {
        Wallet existingWallet = new Wallet(user);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(existingWallet));

        assertThrows(WalletAlreadyExistsException.class,
                () -> walletService.create(request));

        verify(walletRepository, never()).save(any());
    }

    @Test
    void givenInvalidUser_whenCreatingWallet_thenUserNotFoundExceptionIsThrown() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> walletService.create(request));

        verify(walletRepository, never()).save(any());
    }

    @Test
    void givenExistingUserAndWallet_whenCreatingWalletAsset_thenWalletAssetIsAddedToWallet() {
        wallet.setAssets(new ArrayList<>());

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(wallet));

        WalletResponse actualResponse = walletService.createAsset(USERNAME, assetRequest);

        assertNotNull(actualResponse);
        assertEquals(1, wallet.getAssets().size());

        WalletAsset addedAsset = wallet.getAssets().get(0);

        assertEquals(BTC, addedAsset.getSymbol());
        assertEquals(wallet, addedAsset.getWallet());
        verify(walletAssetRepository).save(addedAsset);
    }

    @Test
    void givenUserHasNoWallet_whenCreatingWalletAsset_thenWalletNotFoundExceptionIsThrown() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletService.createAsset(USERNAME, assetRequest));

        verify(walletAssetRepository, never()).save(any());
    }

    @Test
    void givenInvalidUser_whenCreatingWalletAsset_thenUserNotFoundExceptionIsThrown() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> walletService.createAsset(USERNAME, assetRequest));

        verify(walletAssetRepository, never()).save(any());
    }

}