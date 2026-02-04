package com.example.ecommerce.order.service;

import com.example.ecommerce.order.dto.*;
import com.example.ecommerce.order.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream().map(i ->
                OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct().getId())
                        .productName(i.getProductNameSnapshot())
                        .sku(i.getSkuSnapshot())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .lineTotal(i.getLineTotal())
                        .build()
        ).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .paymentReference(order.getPayment() != null ? order.getPayment().getReference() : null)
                .paymentStatus(order.getPayment() != null ? order.getPayment().getStatus().name() : null)
                .items(items)
                .build();
    }

    public OrderSummaryResponse toSummary(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .paymentReference(order.getPayment() != null ? order.getPayment().getReference() : null)
                .paymentStatus(order.getPayment() != null ? order.getPayment().getStatus().name() : null)
                .build();
    }
}