package com.dex.orderengine.repository;

import com.dex.orderengine.model.Order;
import com.dex.orderengine.model.OrderStatus;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);
    List<Order> findTop100ByOrderByCreatedAtDesc();
    List<Order> findByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
    long countByStatusIn(List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.retryCount < :maxRetries")
    List<Order> findRetryableOrders(OrderStatus status, int maxRetries);
}
