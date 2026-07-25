package com.swisspost.cryptowallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WalletAssetRequest {

    @NotBlank(message = "symbol is required")
    private String symbol;

    @NotNull(message = "quantity is required")
    @PositiveOrZero(message = "quantity must be bigger than zero")
    private BigDecimal quantity;

    @NotNull
    @PositiveOrZero(message = "purchasePrice must be bigger or equals to zero")
    private BigDecimal purchasePrice;

    @NotNull(message = "purchaseDate is required")
    private LocalDateTime purchaseDate;
}
