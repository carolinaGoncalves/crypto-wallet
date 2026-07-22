package com.swisspost.cryptowallet.scheduling;

import com.swisspost.cryptowallet.repository.WalletRepository;
import com.swisspost.cryptowallet.service.PriceService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PriceHistoryScheduling {

    private final PriceService priceService;
    private final WalletRepository walletRepository;

    private final ExecutorService executorService;

    private static final Logger log = LoggerFactory.getLogger(PriceHistoryScheduling.class);

    public PriceHistoryScheduling(PriceService priceService, WalletRepository walletRepository,
                                  @Value("${schedule.price.thread.pool.size}") int threadPoolSize) {
        this.priceService = priceService;
        this.walletRepository = walletRepository;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    @Scheduled(cron = "${scheduler.price.cron}")
    public void insertPriceAssets(){
        List<String> distinctSymbols = walletRepository.findDistinctSymbols();

        if (distinctSymbols.isEmpty()) {
            log.info("No wallets available yet");
            return;
        }

        for (String symbol : distinctSymbols) {
            executorService.submit(() -> {
                try {
                    priceService.getAndInsertPrices(symbol);
                } catch (Exception e) {
                    log.error("Failed to insert price for {}: {}", symbol, e.getMessage());
                }
            });
        }

    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        log.info("Executor service shutdown");

    }
}
