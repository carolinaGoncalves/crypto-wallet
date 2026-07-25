package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.request.WalletAssetRequest;
import com.swisspost.cryptowallet.dto.request.WalletRequest;
import com.swisspost.cryptowallet.dto.response.WalletResponse;
import com.swisspost.cryptowallet.entity.User;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.entity.WalletAsset;
import com.swisspost.cryptowallet.exception.InvalidDateException;
import com.swisspost.cryptowallet.exception.WalletAlreadyExistsException;
import com.swisspost.cryptowallet.exception.WalletNotFoundException;
import com.swisspost.cryptowallet.repository.UserRepository;
import com.swisspost.cryptowallet.repository.WalletAssetRepository;
import com.swisspost.cryptowallet.repository.WalletRepository;
import com.swisspost.cryptowallet.utils.UserValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.swisspost.cryptowallet.dto.request.WalletRequest.mapWalletAssetDtoToWalletAsset;
import static com.swisspost.cryptowallet.dto.response.WalletResponse.mapWalletToWalletResponse;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletAssetRepository walletAssetRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletAssetRepository walletAssetRepository, WalletRepository walletRepository, UserRepository userRepository){
        this.walletAssetRepository = walletAssetRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public WalletResponse create(WalletRequest walletRequest){
        String username = walletRequest.getUsername();
        log.debug("Creating wallet for user: {}", username);

        User user = UserValidationUtils.getUserIfExists(userRepository, username);

        if(walletRepository.findByUserUsername(username).isPresent()){
            log.warn("User {} have already a wallet", username);
            throw new WalletAlreadyExistsException("User already has a wallet");
        }

        Wallet wallet = new Wallet(user);
        walletRepository.save(wallet);
        log.debug("Wallet saved for user: {}", username);

        return mapWalletToWalletResponse(wallet);
    }

    public WalletResponse createAsset(String username, WalletAssetRequest walletAssetRequest) {
        UserValidationUtils.getUserIfExists(userRepository, username);
        log.debug("Creating wallet asset for user: {}", username);

        Wallet wallet = getWallet(username);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (walletAssetRequest.getPurchaseDate().isAfter(now)) {
            throw new InvalidDateException("Purchase date cannot be in the future: " + walletAssetRequest.getPurchaseDate());
        }

        WalletAsset walletAsset = mapWalletAssetDtoToWalletAsset(walletAssetRequest);
        wallet.getAssets().add(walletAsset);
        walletAsset.setWallet(wallet);

        walletAssetRepository.save(walletAsset);
        log.info("Wallet asset {} added to wallet for user {}", walletAsset.getSymbol(), username);

        return mapWalletToWalletResponse(wallet);
    }



    public WalletResponse getByUsername(String username){
        UserValidationUtils.getUserIfExists(userRepository, username);
        log.debug("Getting wallet for user: {}", username);

        Wallet wallet = getWallet(username);

        return mapWalletToWalletResponse(wallet);

    }

    private Wallet getWallet(String username) {
        return walletRepository.findByUserUsername(username)
                .orElseThrow(() -> new WalletNotFoundException(username));
    }


}

