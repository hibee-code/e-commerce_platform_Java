package com.example.ecommerce.order.service;

import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.order.dto.*;
import com.example.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listForUser(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdWithPayment(userId, pageable)
                .map(orderMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public OrderResponse detailsForUser(UUID userId, UUID orderId) {
        var order = orderRepository.findByIdForUserWithItems(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderMapper.toResponse(order);
    }
}
