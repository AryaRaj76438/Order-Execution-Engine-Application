package com.dex.orderengine.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    @NotBlank(message = "Token in is required")
    private String tokenIn;

    @NotBlank(message = "Token out is required")
    private String tokenOut;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @DecimalMin(value = "0.001", message = "Slippage must be at least 0.1%")
    @DecimalMax(value = "0.5", message = "Slippage cannot exceed 50%")
    private BigDecimal slippage = new BigDecimal("0.01");
}
