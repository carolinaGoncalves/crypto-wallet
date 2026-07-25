package com.swisspost.cryptowallet.repository;

import com.swisspost.cryptowallet.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    @Query("""
        SELECT priceHistory
        FROM PriceHistory priceHistory
        WHERE priceHistory.symbol IN :symbols
        AND priceHistory.retrievedDate = (
            SELECT MAX(ph.retrievedDate) FROM PriceHistory ph WHERE ph.symbol = priceHistory.symbol
                    and ph.retrievedDate<= :date
        )
        """)
    List<PriceHistory> findLatestPricesByDate(List<String> symbols, LocalDateTime date);
}
