package com.swisspost.cryptowallet.dto.request;

import com.swisspost.cryptowallet.entity.WalletAsset;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletRequest {

    @NotBlank(message = "username is required")
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
