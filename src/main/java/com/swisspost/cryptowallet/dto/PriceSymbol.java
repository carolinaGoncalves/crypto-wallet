package com.swisspost.cryptowallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PriceSymbol {

    private final long timestamp;
    private final List<String> data;


}
