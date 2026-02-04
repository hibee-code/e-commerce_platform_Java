package com.example.ecommerce.order.dto;

import com.example.ecommerce.order.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private UUID id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;

    private String paymentReference;
    private String paymentStatus;

    private List<OrderItemResponse> items;
}
