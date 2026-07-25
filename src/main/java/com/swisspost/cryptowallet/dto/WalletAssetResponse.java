package com.swisspost.cryptowallet.dto;

import com.swisspost.cryptowallet.entity.WalletAsset;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class WalletAssetResponse {

    private String symbol;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private LocalDateTime purchaseDate;


    public static WalletAssetResponse mapFromWalletAsset(WalletAsset asset) {
        return new WalletAssetResponse(
                asset.getSymbol(),
                asset.getQuantity(),
                asset.getPurchasePrice(),
                asset.getPurchaseDate()
        );
    }
}
