package com.swisspost.cryptowallet.repository;

import com.swisspost.cryptowallet.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

}
