package com.dex.orderengine.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data @NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String tokenIn;

    @Column(nullable = false)
    private String tokenOut;

    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal amount;

    @Column(precision = 10, scale = 4)
    private BigDecimal slippage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private DexType selectedDex;

    @Column(precision = 20, scale = 10)
    private BigDecimal executedPrice;

    @Column(precision = 20, scale = 10)
    private BigDecimal raydiumQuote;

    @Column(precision = 20, scale = 10)
    private BigDecimal meteorQuote;

    private String txHash;

    @Column(length = 1000)
    private String errorMessage;

    private Integer retryCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
