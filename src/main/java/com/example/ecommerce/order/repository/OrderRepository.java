package com.example.ecommerce.order.repository;

import com.example.ecommerce.order.entity.Order;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // List (no items fetch; keep light)
    @Query("""
      SELECT o FROM Order o
      LEFT JOIN FETCH o.payment p
      WHERE o.user.id = :userId
    """)
    Page<Order> findByUserIdWithPayment(@Param("userId") UUID userId, Pageable pageable);

    // Details (fetch items + product + payment)
    @Query("""
      SELECT o FROM Order o
      LEFT JOIN FETCH o.payment pay
      LEFT JOIN FETCH o.items i
      LEFT JOIN FETCH i.product pr
      WHERE o.id = :orderId AND o.user.id = :userId
    """)
    Optional<Order> findByIdForUserWithItems(@Param("orderId") UUID orderId, @Param("userId") UUID userId);
}