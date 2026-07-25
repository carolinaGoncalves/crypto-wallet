package com.swisspost.cryptowallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AssetValuationResponse {

    private String symbol;
    private BigDecimal quantity;
    private BigDecimal currentSymbolPrice;
    private BigDecimal currentAssetValue;


}
