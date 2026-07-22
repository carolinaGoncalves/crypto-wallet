package com.swisspost.cryptowallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime retrievedDate;

    public PriceHistory(String symbol, BigDecimal price, LocalDateTime retrievedDate) {
        this.symbol = symbol;
        this.price = price;
        this.retrievedDate = retrievedDate;
    }
}
