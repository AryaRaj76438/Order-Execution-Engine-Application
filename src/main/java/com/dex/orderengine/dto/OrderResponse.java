package com.dex.orderengine.dto;

import com.dex.orderengine.model.DexType;
import com.dex.orderengine.model.OrderStatus;
import com.dex.orderengine.model.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String orderId;
    private String tokenIn;
    private String tokenOut;
    private BigDecimal amount;
    private BigDecimal slippage;
    private OrderType orderType;
    private OrderStatus status;
    private DexType selectedDex;
    private BigDecimal executedPrice;
    private BigDecimal raydiumQuote;
    private BigDecimal meteorQuote;
    private String txHash;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String message;
}
