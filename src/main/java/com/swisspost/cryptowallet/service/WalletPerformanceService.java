package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.query.SymbolQuantityAsset;
import com.swisspost.cryptowallet.dto.query.SymbolTotalInvestment;
import com.swisspost.cryptowallet.dto.query.WalletPriceData;
import com.swisspost.cryptowallet.dto.response.AssetPerformanceResponse;
import com.swisspost.cryptowallet.dto.response.WalletPerformanceResponse;
import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.exception.WalletNotFoundException;
import com.swisspost.cryptowallet.repository.PriceHistoryRepository;
import com.swisspost.cryptowallet.repository.UserRepository;
import com.swisspost.cryptowallet.repository.WalletAssetRepository;
import com.swisspost.cryptowallet.repository.WalletRepository;
import com.swisspost.cryptowallet.utils.UserValidationUtils;
import com.swisspost.cryptowallet.utils.WalletPriceDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletPerformanceService {
    private static final Logger log = LoggerFactory.getLogger(WalletPerformanceService.class);

    private final WalletRepository walletRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final WalletAssetRepository walletAssetRepository;
    private final UserRepository userRepository;

    public WalletPerformanceService(WalletRepository walletRepository, PriceHistoryRepository priceHistoryRepository, WalletAssetRepository walletAssetRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.walletAssetRepository = walletAssetRepository;
        this.userRepository = userRepository;
    }

    public WalletPerformanceResponse getWalletPerformancesByUsername(String username) {
        log.debug("Starting wallet performances for user: {}", username);

        UserValidationUtils.getUserIfExists(userRepository, username);

        Wallet wallet = walletRepository.findByUserUsername(username)
                .orElseThrow(() -> new WalletNotFoundException(username));

        List<SymbolTotalInvestment> totalInvestments = walletAssetRepository.sumTotalInvestmentGroupedBySymbol(wallet.getId());
        log.debug("Total investments for user={}: {}", username, totalInvestments);

        WalletPriceData walletPriceData = WalletPriceDataUtils.buildWalletPriceData(walletAssetRepository,
                priceHistoryRepository, wallet, LocalDateTime.now(ZoneOffset.UTC));

        Map<String, BigDecimal> investedValueBySymbol = buildInvestedBySymbol(totalInvestments);
        Map<String, BigDecimal> currentValueBySymbol = buildCurrentValueBySymbol(walletPriceData);

        WalletPerformanceResponse walletPerformanceResponse = buildWalletPerformanceResponse(currentValueBySymbol, investedValueBySymbol, username);
        log.info("Wallet performances complete for user:{} with best asset:{} and worst asset:{}",
                username, walletPerformanceResponse.getBestPerforming(), walletPerformanceResponse.getWorstPerforming());

        return walletPerformanceResponse;

    }

    private static Map<String, BigDecimal> buildCurrentValueBySymbol(WalletPriceData walletPriceData) {
        Map<String, BigDecimal> currentValueBySymbol = new HashMap<>();

        for (SymbolQuantityAsset symbolQuantityAsset : walletPriceData.getQuantitiesBySymbol()) {
            BigDecimal price = walletPriceData.getPriceBySymbol().get(symbolQuantityAsset.getSymbol());

            if (price != null) {
                BigDecimal currentValue = symbolQuantityAsset.getQuantity().multiply(price);
                currentValueBySymbol.put(symbolQuantityAsset.getSymbol(), currentValue);
            }else{
                log.warn("No price available for symbol: {}", symbolQuantityAsset.getSymbol());
            }
        }

        return currentValueBySymbol;
    }

    private static Map<String, BigDecimal> buildInvestedBySymbol(List<SymbolTotalInvestment> totalInvestments) {
        Map<String, BigDecimal> investedBySymbol = new HashMap<>();
        for (SymbolTotalInvestment symbolTotalInvestment : totalInvestments) {
            investedBySymbol.put(symbolTotalInvestment.getSymbol(), symbolTotalInvestment.getTotalInvestment());
        }
        return investedBySymbol;
    }

    private WalletPerformanceResponse buildWalletPerformanceResponse(Map<String, BigDecimal> currentValueBySymbol,
                                                                     Map<String, BigDecimal> investedValueBySymbol,
                                                                     String username) {
        List<AssetPerformanceResponse> assetPerformanceResponses = new ArrayList<>();
        AssetPerformanceResponse bestAssetPerformance = null;
        AssetPerformanceResponse worstAssetPerformance = null;

        for (String symbol : currentValueBySymbol.keySet()) {
            BigDecimal percentage = getPercentage(currentValueBySymbol, investedValueBySymbol, symbol);

            AssetPerformanceResponse performance = new AssetPerformanceResponse(symbol, percentage);
            assetPerformanceResponses.add(performance);

            if (bestAssetPerformance == null || percentage.compareTo(bestAssetPerformance.getPerformancePercentage()) > 0) {
                bestAssetPerformance = performance;
            }
            if (worstAssetPerformance == null || percentage.compareTo(worstAssetPerformance.getPerformancePercentage()) < 0) {
                worstAssetPerformance = performance;
            }
        }

        return new WalletPerformanceResponse(username, bestAssetPerformance, worstAssetPerformance, assetPerformanceResponses);
    }

    private static BigDecimal getPercentage(Map<String, BigDecimal> currentValueBySymbol, Map<String, BigDecimal> investedValueBySymbol, String symbol) {
        BigDecimal current = currentValueBySymbol.get(symbol);
        BigDecimal invested = investedValueBySymbol.get(symbol);

        if (invested == null || invested.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Invested amount is zero for symbol: {}", symbol);
            return BigDecimal.ZERO;
        }

        BigDecimal gainOrLoss = current.subtract(invested);
        return gainOrLoss
                .divide(invested, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }


}
