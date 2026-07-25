package com.swisspost.cryptowallet.repository;

import com.swisspost.cryptowallet.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    @Query("""
        SELECT ph FROM PriceHistory ph
        WHERE ph.symbol IN :symbols
        AND ph.retrievedDate = (
            SELECT MAX(ph2.retrievedDate) FROM PriceHistory ph2 WHERE ph2.symbol = ph.symbol
                    and ph2.retrievedDate<= :date
        )
        """)
    List<PriceHistory> findLatestPricesByDate(List<String> symbols, LocalDateTime date);
}
