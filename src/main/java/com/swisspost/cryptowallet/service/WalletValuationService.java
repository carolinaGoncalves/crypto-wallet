package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.AssetValuationResponse;
import com.swisspost.cryptowallet.dto.SymbolQuantityAsset;
import com.swisspost.cryptowallet.dto.WalletValueResponse;
import com.swisspost.cryptowallet.entity.PriceHistory;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.exception.InvalidDateException;
import com.swisspost.cryptowallet.exception.WalletNotFoundException;
import com.swisspost.cryptowallet.repository.PriceHistoryRepository;
import com.swisspost.cryptowallet.repository.WalletAssetRepository;
import com.swisspost.cryptowallet.repository.WalletRepository;
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
import java.util.stream.Collectors;

@Service
public class WalletValuationService {

    private final WalletAssetRepository walletAssetRepository;
    private final WalletRepository walletRepository;
    private final PriceHistoryRepository priceHistoryRepository;


    public WalletValuationService(WalletAssetRepository walletAssetRepository, WalletRepository walletRepository, PriceHistoryRepository priceHistoryRepository) {
        this.walletAssetRepository = walletAssetRepository;
        this.walletRepository = walletRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Transactional
    public WalletValueResponse getWalletValueByUsername(String username, LocalDate date){
        LocalDateTime filterDate = date.atTime(LocalTime.MAX);

        if(filterDate.isAfter(LocalDateTime.now(ZoneOffset.UTC))){
            throw new InvalidDateException("Date cannot be in the future: "+filterDate.toLocalDate());
        }

        Wallet wallet = walletRepository.findByUserUsername(username)
                .orElseThrow(() -> new WalletNotFoundException(username));

        if (wallet.getAssets().isEmpty()) {
            return WalletValueResponse.mapWalletToWalletResponse(wallet,new BigDecimal("0.0"), new ArrayList<>(),
                    filterDate);
        }

        List<SymbolQuantityAsset> quantitiesBySymbol =
                walletAssetRepository.sumQuantityGroupedBySymbolAndByPurchaseDate(wallet.getId(), filterDate);

        List<String> symbols = quantitiesBySymbol.stream()
                .map(SymbolQuantityAsset::getSymbol)
                .toList();

        Map<String, BigDecimal> priceBySymbol = priceHistoryRepository.findLatestPricesByDate(symbols, filterDate).stream()
                .collect(Collectors.toMap(PriceHistory::getSymbol, PriceHistory::getPrice));

        List<AssetValuationResponse> assetValues = mapAssetValuationResponse(quantitiesBySymbol, priceBySymbol);

        BigDecimal currentValue = computeCurrentValue(assetValues);

        return WalletValueResponse.mapWalletToWalletResponse(wallet,currentValue, assetValues, filterDate);
    }

    private BigDecimal computeCurrentValue(List<AssetValuationResponse> assetValues) {
        BigDecimal totalValue = BigDecimal.ZERO;
        for (AssetValuationResponse asset : assetValues) {
            if(asset.getCurrentAssetValue()==null){
                continue;
            }
            totalValue = totalValue.add(asset.getCurrentAssetValue());
        }

        return totalValue;
    }

    private List<AssetValuationResponse> mapAssetValuationResponse(List<SymbolQuantityAsset> quantitiesBySymbol, Map<String, BigDecimal> priceBySymbol) {
        return quantitiesBySymbol.stream()
                .map(symbolQuantityAsset -> {
                    BigDecimal price = priceBySymbol.get(symbolQuantityAsset.getSymbol());
                    BigDecimal value = price!=null? symbolQuantityAsset.getQuantity().multiply(price):null;
                    return new AssetValuationResponse(symbolQuantityAsset.getSymbol(), symbolQuantityAsset.getQuantity(), price, value);
                })
                .toList();
    }

}
