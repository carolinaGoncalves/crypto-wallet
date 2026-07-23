package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.WalletAssetRequest;
import com.swisspost.cryptowallet.dto.WalletAssetResponse;
import com.swisspost.cryptowallet.dto.WalletRequest;
import com.swisspost.cryptowallet.dto.WalletResponse;
import com.swisspost.cryptowallet.entity.User;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.entity.WalletAsset;
import com.swisspost.cryptowallet.exception.UserNotFoundException;
import com.swisspost.cryptowallet.exception.WalletNotFoundException;
import com.swisspost.cryptowallet.repository.UserRepository;
import com.swisspost.cryptowallet.repository.WalletAssetRepository;
import com.swisspost.cryptowallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.swisspost.cryptowallet.dto.WalletRequest.mapWalletAssetDtoToWalletAsset;
import static com.swisspost.cryptowallet.dto.WalletResponse.mapWalletToWalletResponse;

@Service
public class WalletService {

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
        Optional<User> user = userRepository.findByUsername(username);

        if(user.isEmpty()){
            throw new UserNotFoundException("User not found for "+username);
        }

        Wallet wallet = new Wallet(user.get());
        walletRepository.save(wallet);

        return mapWalletToWalletResponse(wallet);
    }

    public WalletResponse createAsset(String username, WalletAssetRequest walletAssetRequest) {
        Wallet wallet = walletRepository.findByUserUsername(username)
                .orElseThrow(() -> new WalletNotFoundException(username));

        WalletAsset walletAsset = mapWalletAssetDtoToWalletAsset(walletAssetRequest);
        wallet.getAssets().add(walletAsset);
        walletAsset.setWallet(wallet);

        walletAssetRepository.save(walletAsset);

        return mapWalletToWalletResponse(wallet);
    }



    public WalletResponse getByUsername(String username){
        Wallet wallet = walletRepository.findByUserUsername(username)
                .orElseThrow(() -> new WalletNotFoundException(username));

        return mapWalletToWalletResponse(wallet);

    }

}

