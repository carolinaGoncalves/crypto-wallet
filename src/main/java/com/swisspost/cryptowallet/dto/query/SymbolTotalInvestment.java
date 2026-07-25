package com.swisspost.cryptowallet.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SymbolTotalInvestment {

    private String symbol;
    private BigDecimal totalInvestment;
}
