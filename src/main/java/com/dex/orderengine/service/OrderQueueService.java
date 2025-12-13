package com.dex.orderengine.service;

import com.dex.orderengine.model.Order;
import com.dex.orderengine.model.OrderStatus;
import com.dex.orderengine.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueueService {

    private static final int MAX_CONCURRENT_ORDERS = 10;
    private static final int MAX_QUEUE_SIZE = 100;

    private final LinkedBlockingQueue<String> orderQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final Map<String, Order> activeOrders = new ConcurrentHashMap<>();
    private final AtomicInteger processingCount = new AtomicInteger(0);

    private final OrderRepository orderRepository;

    public boolean enqueue(Order order) {
        if (orderQueue.size() >= MAX_QUEUE_SIZE) {
            log.warn("Queue is full, cannot accept more orders");
            return false;
        }

        activeOrders.put(order.getId(), order);
        boolean added = orderQueue.offer(order.getId());

        if (added) {
            log.info("Order {} added to queue. Queue size: {}, Processing: {}",
                    order.getId(), orderQueue.size(), processingCount.get());
        }

        return added;
    }

    public String pollNext() {
        if (processingCount.get() >= MAX_CONCURRENT_ORDERS) {
            return null;
        }

        String orderId = orderQueue.poll();
        if (orderId != null) {
            processingCount.incrementAndGet();
            log.debug("Dequeued order {}. Queue size: {}, Processing: {}",
                    orderId, orderQueue.size(), processingCount.get());
        }
        return orderId;
    }

    public void markCompleted(String orderId) {
        activeOrders.remove(orderId);
        processingCount.decrementAndGet();
        log.info("Order {} completed. Queue size: {}, Processing: {}",
                orderId, orderQueue.size(), processingCount.get());
    }

    public void markFailed(String orderId) {
        activeOrders.remove(orderId);
        processingCount.decrementAndGet();
        log.info("Order {} failed. Queue size: {}, Processing: {}",
                orderId, orderQueue.size(), processingCount.get());
    }

    public Order getActiveOrder(String orderId) {
        return activeOrders.get(orderId);
    }

    public int getQueueSize() {
        return orderQueue.size();
    }

    public int getProcessingCount() {
        return processingCount.get();
    }

    public int getActiveOrdersCount() {
        return activeOrders.size();
    }

    public Map<String, Object> getQueueStats() {
        return Map.of(
                "queueSize", orderQueue.size(),
                "processingCount", processingCount.get(),
                "activeOrders", activeOrders.size(),
                "maxConcurrent", MAX_CONCURRENT_ORDERS,
                "maxQueueSize", MAX_QUEUE_SIZE
        );
    }
}
