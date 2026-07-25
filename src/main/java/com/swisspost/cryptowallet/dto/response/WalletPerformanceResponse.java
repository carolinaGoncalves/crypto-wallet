package com.swisspost.cryptowallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WalletPerformanceResponse {

    private String username;
    private AssetPerformanceResponse bestPerforming;
    private AssetPerformanceResponse worstPerforming;
    private List<AssetPerformanceResponse> assets;

}
