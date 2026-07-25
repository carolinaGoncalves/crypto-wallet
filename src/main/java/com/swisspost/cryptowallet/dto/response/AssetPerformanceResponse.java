package com.swisspost.cryptowallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AssetPerformanceResponse {

    private String symbol;
    private BigDecimal performancePercentage;

}
