package com.dex.orderengine.service;

import com.dex.orderengine.dto.DexQuote;
import com.dex.orderengine.dto.ExecutionResult;
import com.dex.orderengine.dto.OrderRequest;
import com.dex.orderengine.dto.OrderResponse;
import com.dex.orderengine.model.Order;
import com.dex.orderengine.model.OrderStatus;
import com.dex.orderengine.model.OrderType;
import com.dex.orderengine.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExecutionService {

    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;

    private final OrderRepository orderRepository;
    private final OrderQueueService queueService;
    private final MockDexRoutingService dexRoutingService;
    private final WebSocketNotificationService notificationService;

    @Transactional
    public OrderResponse submitOrder(OrderRequest request) {
        log.info("Submitting new market order: {} {} -> {}",
                request.getAmount(), request.getTokenIn(), request.getTokenOut());

        Order order = Order.builder()
                .tokenIn(request.getTokenIn())
                .tokenOut(request.getTokenOut())
                .amount(request.getAmount())
                .slippage(request.getSlippage())
                .orderType(OrderType.MARKET)
                .status(OrderStatus.PENDING)
                .retryCount(0)
                .build();

        order = orderRepository.save(order);
        log.info("Order created with ID: {}", order.getId());

        boolean queued = queueService.enqueue(order);
        if (!queued) {
            order.setStatus(OrderStatus.FAILED);
            order.setErrorMessage("Queue is full, please try again later");
            orderRepository.save(order);

            return OrderResponse.builder()
                    .orderId(order.getId())
                    .status(OrderStatus.FAILED)
                    .message("Queue is full, please try again later")
                    .build();
        }

        notificationService.notifyOrderStatus(order.getId(), OrderStatus.PENDING,
                "Order received and queued for execution");

        return OrderResponse.builder()
                .orderId(order.getId())
                .tokenIn(order.getTokenIn())
                .tokenOut(order.getTokenOut())
                .amount(order.getAmount())
                .slippage(order.getSlippage())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .message("Order queued successfully. Connect to WebSocket for live updates.")
                .build();
    }


    private void handleRetry(Order order, String errorMessage) {
        order.setRetryCount(order.getRetryCount() + 1);

        if (order.getRetryCount() >= MAX_RETRY_COUNT) {
            order.setStatus(OrderStatus.FAILED);
            order.setErrorMessage(errorMessage);
            order.setCompletedAt(LocalDateTime.now());
            orderRepository.save(order);

            notificationService.notifyFailed(order.getId(), errorMessage);
            queueService.markFailed(order.getId());

            log.error("Order {} failed after {} retries: {}",
                    order.getId(), MAX_RETRY_COUNT, errorMessage);
        } else {
            long delay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, order.getRetryCount() - 1);
            log.info("Retrying order {} (attempt {}/{}) after {}ms",
                    order.getId(), order.getRetryCount(), MAX_RETRY_COUNT, delay);

            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            notificationService.notifyOrderStatus(order.getId(), OrderStatus.PENDING,
                    "Retrying... (attempt " + order.getRetryCount() + "/" + MAX_RETRY_COUNT + ")");

            queueService.enqueue(order);
        }
    }

    private void handleExecutionFailure(String orderId, String errorMessage) {
        Optional<Order> optOrder = orderRepository.findById(orderId);
        if (optOrder.isPresent()) {
            Order order = optOrder.get();
            handleRetry(order, errorMessage);
        } else {
            queueService.markFailed(orderId);
        }
    }

    public Optional<OrderResponse> getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(this::mapToResponse);
    }

    public List<OrderResponse> getRecentOrders() {
        return orderRepository.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .tokenIn(order.getTokenIn())
                .tokenOut(order.getTokenOut())
                .amount(order.getAmount())
                .slippage(order.getSlippage())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .selectedDex(order.getSelectedDex())
                .executedPrice(order.getExecutedPrice())
                .raydiumQuote(order.getRaydiumQuote())
                .meteorQuote(order.getMeteorQuote())
                .txHash(order.getTxHash())
                .errorMessage(order.getErrorMessage())
                .createdAt(order.getCreatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
}
