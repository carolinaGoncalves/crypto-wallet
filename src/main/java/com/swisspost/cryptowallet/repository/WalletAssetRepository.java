package com.swisspost.cryptowallet.repository;

import com.swisspost.cryptowallet.entity.WalletAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WalletAssetRepository extends JpaRepository<WalletAsset, UUID> {

    @Query("SELECT DISTINCT wa.symbol FROM WalletAsset wa")
    List<String> findDistinctSymbols();

}
