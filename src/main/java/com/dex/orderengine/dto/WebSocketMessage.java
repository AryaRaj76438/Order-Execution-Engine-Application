package com.dex.orderengine.dto;

import com.dex.orderengine.model.DexType;
import com.dex.orderengine.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
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
public class WebSocketMessage {
    private String orderId;
    private OrderStatus status;
    private String message;
    private DexType selectedDex;
    private BigDecimal raydiumQuote;
    private BigDecimal meteorQuote;
    private BigDecimal executedPrice;
    private String txHash;
    private String error;
    private LocalDateTime timestamp;
}
