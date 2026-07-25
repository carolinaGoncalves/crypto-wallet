package com.swisspost.cryptowallet.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class WalletPriceData {

    private List<SymbolQuantityAsset> quantitiesBySymbol;
    private Map<String, BigDecimal> priceBySymbol;

}
