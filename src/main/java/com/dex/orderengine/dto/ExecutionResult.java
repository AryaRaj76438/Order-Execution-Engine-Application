package com.dex.orderengine.dto;


import com.dex.orderengine.model.DexType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionResult {
    private boolean success;
    private String txHash;
    private BigDecimal executedPrice;
    private DexType dex;
    private String errorMessage;
}


