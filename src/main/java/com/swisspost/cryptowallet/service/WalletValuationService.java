package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.response.AssetValuationResponse;
import com.swisspost.cryptowallet.dto.query.SymbolQuantityAsset;
import com.swisspost.cryptowallet.dto.query.WalletPriceData;
import com.swisspost.cryptowallet.dto.response.WalletValueResponse;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.exception.InvalidDateException;
import com.swisspost.cryptowallet.exception.WalletNotFoundException;
import com.swisspost.cryptowallet.repository.PriceHistoryRepository;
import com.swisspost.cryptowallet.repository.UserRepository;
import com.swisspost.cryptowallet.repository.WalletAssetRepository;
import com.swisspost.cryptowallet.repository.WalletRepository;
import com.swisspost.cryptowallet.utils.UserValidationUtils;
import com.swisspost.cryptowallet.utils.WalletPriceDataUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WalletValuationService {

    private final WalletAssetRepository walletAssetRepository;
    private final WalletRepository walletRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final UserRepository userRepository;


    public WalletValuationService(WalletAssetRepository walletAssetRepository, WalletRepository walletRepository, PriceHistoryRepository priceHistoryRepository, UserRepository userRepository) {
        this.walletAssetRepository = walletAssetRepository;
        this.walletRepository = walletRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WalletValueResponse getWalletValueByUsername(String username, LocalDate date){
        UserValidationUtils.getUserIfExists(userRepository, username);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (date.isAfter(today)) {
            throw new InvalidDateException("Date cannot be in the future: " + date);
        }

        LocalDateTime filterDate = date.atTime(LocalTime.MAX);

        Wallet wallet = walletRepository.findByUserUsername(username)
                .orElseThrow(() -> new WalletNotFoundException(username));

        WalletValueResponse walletValueResponse = getWalletValueResponse(wallet, filterDate);

        if (walletValueResponse != null){
            return walletValueResponse;
        }

        WalletPriceData walletPriceData = WalletPriceDataUtils.buildWalletPriceData(walletAssetRepository,
                priceHistoryRepository, wallet, filterDate);

        List<AssetValuationResponse> assetValues = mapAssetValuationResponse(walletPriceData.getQuantitiesBySymbol(), walletPriceData.getPriceBySymbol());

        BigDecimal currentValue = computeCurrentValue(assetValues);

        return WalletValueResponse.mapWalletToWalletResponse(wallet,currentValue, assetValues, filterDate);
    }

    static WalletValueResponse getWalletValueResponse(Wallet wallet, LocalDateTime filterDate) {
        if (wallet.getAssets().isEmpty()) {
            return WalletValueResponse.mapWalletToWalletResponse(wallet, new BigDecimal("0.0"), new ArrayList<>(),
                    filterDate);
        }
        return null;
    }

    private BigDecimal computeCurrentValue(List<AssetValuationResponse> assetValues) {
        BigDecimal totalValue = BigDecimal.ZERO;
        for (AssetValuationResponse asset : assetValues) {
            if(asset.getCurrentAssetValue()==null){
                asset.setCurrentAssetValue(new BigDecimal("0.0"));
            }
            totalValue = totalValue.add(asset.getCurrentAssetValue());
        }

        return totalValue;
    }

    private List<AssetValuationResponse> mapAssetValuationResponse(List<SymbolQuantityAsset> quantitiesBySymbol, Map<String, BigDecimal> priceBySymbol) {
        return quantitiesBySymbol.stream()
                .map(symbolQuantityAsset -> {
                    BigDecimal price = priceBySymbol.get(symbolQuantityAsset.getSymbol());
                    BigDecimal value = price!=null? symbolQuantityAsset.getQuantity().multiply(price):new BigDecimal("0.0");
                    return new AssetValuationResponse(symbolQuantityAsset.getSymbol(), symbolQuantityAsset.getQuantity(), price!=null?price:new BigDecimal("0.0"), value);
                })
                .toList();
    }

}
