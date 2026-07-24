package com.swisspost.cryptowallet.repository;

import com.swisspost.cryptowallet.dto.SymbolQuantityAsset;
import com.swisspost.cryptowallet.entity.WalletAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WalletAssetRepository extends JpaRepository<WalletAsset, UUID> {

    @Query("SELECT DISTINCT wa.symbol FROM WalletAsset wa")
    List<String> findDistinctSymbols();


    @Query("""
        SELECT new com.swisspost.cryptowallet.dto.SymbolQuantityAsset(wa.symbol, SUM(wa.quantity))
        FROM WalletAsset wa
        WHERE wa.wallet.id = :walletId and wa.purchaseDate<= :date
        GROUP BY wa.symbol
        """)
    List<SymbolQuantityAsset> sumQuantityGroupedBySymbolAndByPurchaseDate(UUID walletId, LocalDateTime date);


}
