package com.dex.orderengine.controller;

import com.dex.orderengine.dto.OrderRequest;
import com.dex.orderengine.dto.OrderResponse;
import com.dex.orderengine.service.OrderExecutionService;
import com.dex.orderengine.service.OrderQueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderExecutionService orderExecutionService;
    private final OrderQueueService queueService;

    @PostMapping("/execute")
    public ResponseEntity<OrderResponse> executeOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Received order execution request: {}", request);
        OrderResponse response = orderExecutionService.submitOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return orderExecutionService.getOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getRecentOrders() {
        return ResponseEntity.ok(orderExecutionService.getRecentOrders());
    }

    @GetMapping("/queue/stats")
    public ResponseEntity<Map<String, Object>> getQueueStats() {
        return ResponseEntity.ok(queueService.getQueueStats());
    }
}
