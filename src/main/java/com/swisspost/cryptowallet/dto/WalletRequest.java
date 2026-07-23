package com.swisspost.cryptowallet.dto;

import com.swisspost.cryptowallet.entity.WalletAsset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class WalletRequest {

    @NonNull
    private String username;


    public static WalletAsset mapWalletAssetDtoToWalletAsset(WalletAssetRequest walletAssetRequest){
        return new WalletAsset(
                walletAssetRequest.getSymbol().toUpperCase(),
                walletAssetRequest.getQuantity(),
                walletAssetRequest.getPurchasePrice(),
                walletAssetRequest.getPurchaseDate()
        );
    }
}
