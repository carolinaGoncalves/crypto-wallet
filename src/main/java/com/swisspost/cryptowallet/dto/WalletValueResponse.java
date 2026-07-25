package com.swisspost.cryptowallet.dto;

import com.swisspost.cryptowallet.entity.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class WalletValueResponse {

    private String username;
    private BigDecimal currentValue;
    private LocalDateTime valuationDate;
    private List<AssetValuationResponse> walletAssets;


    public static WalletValueResponse mapWalletToWalletResponse(Wallet wallet, BigDecimal value,
                                                                List<AssetValuationResponse> assetValues,
                                                                LocalDateTime localDateTime) {
        return new WalletValueResponse(wallet.getUser().getUsername(), value, localDateTime, assetValues);
    }
}
