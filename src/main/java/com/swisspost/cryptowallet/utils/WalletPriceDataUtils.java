package com.swisspost.cryptowallet.utils;

import com.swisspost.cryptowallet.dto.query.SymbolQuantityAsset;
import com.swisspost.cryptowallet.dto.query.WalletPriceData;
import com.swisspost.cryptowallet.entity.PriceHistory;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.repository.PriceHistoryRepository;
import com.swisspost.cryptowallet.repository.WalletAssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletPriceDataUtils {
    private static final Logger log = LoggerFactory.getLogger(WalletPriceDataUtils.class);


    public static WalletPriceData buildWalletPriceData(
            WalletAssetRepository walletAssetRepository,
            PriceHistoryRepository priceHistoryRepository,
            Wallet wallet){

        List<SymbolQuantityAsset> quantitiesBySymbol =
                walletAssetRepository.sumQuantityGroupedBySymbolAndByPurchaseDate(wallet.getId(), LocalDateTime.now(ZoneOffset.UTC));

        if (quantitiesBySymbol.isEmpty()) {
            log.info("Wallet of user {} has no wallet assets", wallet.getUser().getUsername());
            return new WalletPriceData(quantitiesBySymbol, Map.of());
        }

        List<String> symbols = quantitiesBySymbol.stream()
                .map(SymbolQuantityAsset::getSymbol)
                .toList();

        List<PriceHistory> priceHistoryList = priceHistoryRepository.findLatestPricesByDate(symbols, LocalDateTime.now(ZoneOffset.UTC));
        Map<String, BigDecimal> priceBySymbol = new HashMap<>();
        for (PriceHistory priceHistory : priceHistoryList) {
            priceBySymbol.put(priceHistory.getSymbol(), priceHistory.getPrice());
        }

        return new WalletPriceData(quantitiesBySymbol, priceBySymbol);
    }
}
