package com.swisspost.cryptowallet.dto;

import com.swisspost.cryptowallet.entity.Wallet;
import com.swisspost.cryptowallet.entity.WalletAsset;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class WalletResponse {

    private String username;
    private List<WalletAssetResponse> walletAssets;

    public static WalletResponse mapWalletToWalletResponse(Wallet wallet){
        List<WalletAssetResponse> walletAssetResponses = new ArrayList<>();

        List<WalletAsset> assets = wallet.getAssets();
        if(assets !=null && !assets.isEmpty()){
            walletAssetResponses = assets.stream()
                    .map(WalletAssetResponse::mapFromWalletAsset)
                    .toList();
        }

        return new WalletResponse(wallet.getUser().getUsername(), walletAssetResponses);
    }


}
