package com.dex.orderengine.service;

import com.dex.orderengine.dto.DexQuote;
import com.dex.orderengine.dto.ExecutionResult;
import com.dex.orderengine.model.DexType;
import com.dex.orderengine.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MockDexRoutingService {

    private static final Random random = new Random();
    private static final BigDecimal BASE_SOL_PRICE = new BigDecimal("100.00");

    public CompletableFuture<DexQuote> getRaydiumQuote(String tokenIn, String tokenOut, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                Thread.sleep(150 + random.nextInt(100));

                BigDecimal priceMultiplier = new BigDecimal("0.98").add(
                        new BigDecimal(random.nextDouble() * 0.04)
                );
                BigDecimal price = BASE_SOL_PRICE.multiply(priceMultiplier).setScale(6, RoundingMode.HALF_UP);
                BigDecimal fee = new BigDecimal("0.003");
                BigDecimal outputAmount = amount.multiply(price).multiply(BigDecimal.ONE.subtract(fee))
                        .setScale(6, RoundingMode.HALF_UP);

                long responseTime = System.currentTimeMillis() - startTime;

                log.info("Raydium quote for {} {} -> {}: price={}, output={}, fee={}",
                        amount, tokenIn, tokenOut, price, outputAmount, fee);

                return DexQuote.builder()
                        .dexType(DexType.RAYDIUM)
                        .price(price)
                        .fee(fee)
                        .outputAmount(outputAmount)
                        .responseTimeMs(responseTime)
                        .build();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Raydium quote interrupted", e);
            }
        });
    }

    public CompletableFuture<DexQuote> getMeteorQuote(String tokenIn, String tokenOut, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                Thread.sleep(180 + random.nextInt(120));

                BigDecimal priceMultiplier = new BigDecimal("0.97").add(
                        new BigDecimal(random.nextDouble() * 0.05)
                );
                BigDecimal price = BASE_SOL_PRICE.multiply(priceMultiplier).setScale(6, RoundingMode.HALF_UP);
                BigDecimal fee = new BigDecimal("0.002");
                BigDecimal outputAmount = amount.multiply(price).multiply(BigDecimal.ONE.subtract(fee))
                        .setScale(6, RoundingMode.HALF_UP);

                long responseTime = System.currentTimeMillis() - startTime;

                log.info("Meteora quote for {} {} -> {}: price={}, output={}, fee={}",
                        amount, tokenIn, tokenOut, price, outputAmount, fee);

                return DexQuote.builder()
                        .dexType(DexType.METEORA)
                        .price(price)
                        .fee(fee)
                        .outputAmount(outputAmount)
                        .responseTimeMs(responseTime)
                        .build();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Meteora quote interrupted", e);
            }
        });
    }

    public DexQuote selectBestQuote(DexQuote raydiumQuote, DexQuote meteoraQuote) {
        if (raydiumQuote.getOutputAmount().compareTo(meteoraQuote.getOutputAmount()) > 0) {
            log.info("Selected Raydium - Better output: {} vs Meteora: {}",
                    raydiumQuote.getOutputAmount(), meteoraQuote.getOutputAmount());
            return raydiumQuote;
        } else {
            log.info("Selected Meteora - Better output: {} vs Raydium: {}",
                    meteoraQuote.getOutputAmount(), raydiumQuote.getOutputAmount());
            return meteoraQuote;
        }
    }

    public ExecutionResult executeSwap(DexType dex, Order order, DexQuote quote) {
        try {
            log.info("Executing swap on {} for order {}", dex, order.getId());

            int executionDelay = 2000 + random.nextInt(1000);
            Thread.sleep(executionDelay);

            if (random.nextDouble() < 0.05) {
                log.warn("Simulated swap failure for order {}", order.getId());
                return ExecutionResult.builder()
                        .success(false)
                        .dex(dex)
                        .errorMessage("Simulated network error - transaction timeout")
                        .build();
            }

            BigDecimal slippageVariation = new BigDecimal(1 - random.nextDouble() * 0.01);
            BigDecimal executedPrice = quote.getPrice().multiply(slippageVariation)
                    .setScale(6, RoundingMode.HALF_UP);

            String txHash = generateMockTxHash();

            log.info("Swap executed successfully on {} - txHash: {}, price: {}",
                    dex, txHash, executedPrice);

            return ExecutionResult.builder()
                    .success(true)
                    .txHash(txHash)
                    .executedPrice(executedPrice)
                    .dex(dex)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionResult.builder()
                    .success(false)
                    .dex(dex)
                    .errorMessage("Execution interrupted")
                    .build();
        }
    }

    private String generateMockTxHash() {
        StringBuilder hash = new StringBuilder();
        String chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        for (int i = 0; i < 88; i++) {
            hash.append(chars.charAt(random.nextInt(chars.length())));
        }
        return hash.toString();
    }
}
