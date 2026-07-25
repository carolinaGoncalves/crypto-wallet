package com.swisspost.cryptowallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_assets")
@Getter
@Setter
@NoArgsConstructor
public class WalletAsset {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    public WalletAsset(String symbol, BigDecimal quantity, BigDecimal purchasePrice, LocalDateTime purchaseDate) {
        this.symbol=symbol;
        this.quantity=quantity;
        this.purchasePrice=purchasePrice;
        this.purchaseDate=purchaseDate;
    }
}
