package com.swisspost.cryptowallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PriceSymbolResponse {

    private final long timestamp;
    private final List<String> data;


}
