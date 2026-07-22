package com.swisspost.cryptowallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userID;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return Objects.equals(symbol, wallet.symbol) && Objects.equals(quantity, wallet.quantity)
                && Objects.equals(purchasePrice, wallet.purchasePrice)
                && Objects.equals(purchaseDate, wallet.purchaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, quantity, purchasePrice, purchaseDate);
    }
}
