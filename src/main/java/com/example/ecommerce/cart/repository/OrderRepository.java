package com.example.ecommerce.cart.repository;

import com.example.ecommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
      SELECT o FROM Order o
      LEFT JOIN FETCH o.items i
      LEFT JOIN FETCH i.product p
      WHERE o.id = :orderId AND o.user.id = :userId
    """)
    Optional<Order> findByIdForUserWithItems(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    Page<Order> findByUserId(UUID userId, Pageable pageable);
}