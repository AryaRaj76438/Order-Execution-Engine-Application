package com.dex.orderengine.service;

import com.dex.orderengine.dto.WebSocketMessage;
import com.dex.orderengine.model.DexType;
import com.dex.orderengine.model.Order;
import com.dex.orderengine.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyOrderStatus(String orderId, OrderStatus status, String message) {
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .orderId(orderId)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        sendToOrder(orderId, wsMessage);
        sendToAll(wsMessage);
    }

    public void notifyRouting(String orderId, BigDecimal raydiumQuote, BigDecimal meteorQuote, DexType selectedDex) {
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .orderId(orderId)
                .status(OrderStatus.ROUTING)
                .message("Comparing DEX prices - Selected: " + selectedDex.name())
                .raydiumQuote(raydiumQuote)
                .meteorQuote(meteorQuote)
                .selectedDex(selectedDex)
                .timestamp(LocalDateTime.now())
                .build();

        sendToOrder(orderId, wsMessage);
        sendToAll(wsMessage);
    }

    public void notifyConfirmed(Order order) {
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .orderId(order.getId())
                .status(OrderStatus.CONFIRMED)
                .message("Transaction confirmed successfully")
                .selectedDex(order.getSelectedDex())
                .executedPrice(order.getExecutedPrice())
                .txHash(order.getTxHash())
                .raydiumQuote(order.getRaydiumQuote())
                .meteorQuote(order.getMeteorQuote())
                .timestamp(LocalDateTime.now())
                .build();

        sendToOrder(order.getId(), wsMessage);
        sendToAll(wsMessage);
    }

    public void notifyFailed(String orderId, String errorMessage) {
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .orderId(orderId)
                .status(OrderStatus.FAILED)
                .message("Order execution failed")
                .error(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();

        sendToOrder(orderId, wsMessage);
        sendToAll(wsMessage);
    }

    private void sendToOrder(String orderId, WebSocketMessage message) {
        String destination = "/topic/orders/" + orderId;
        log.debug("Sending WebSocket message to {}: {}", destination, message);
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendToAll(WebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/orders", message);
        log.info("Order {} status update: {} - {}", message.getOrderId(), message.getStatus(), message.getMessage());
    }
}
