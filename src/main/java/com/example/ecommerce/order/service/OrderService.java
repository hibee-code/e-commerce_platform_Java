package com.example.ecommerce.order.service;

import com.example.ecommerce.cart.repository.CartRepository;
import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.common.exception.ConflictException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.repository.OrderRepository;
import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.entity.PaymentProvider;
import com.example.ecommerce.payment.entity.PaymentStatus;
import com.example.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Order checkout(UUID userId) {
        var cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Validate stock + create order items with snapshots
        var order = Order.builder()
                .user(cart.getUser())
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (var ci : cart.getItems()) {
            var p = ci.getProduct();

            if (p.getDeletedAt() != null || !p.isActive()) {
                throw new BadRequestException("Product not available: " + p.getName());
            }
            if (p.getStockQuantity() < ci.getQuantity()) {
                throw new ConflictException("Insufficient stock for: " + p.getName());
            }

            // Stock decrement (protected by optimistic lock @Version on Product)
            p.setStockQuantity(p.getStockQuantity() - ci.getQuantity());

            var unit = p.getPrice();
            var lineTotal = unit.multiply(BigDecimal.valueOf(ci.getQuantity()));

            var oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .quantity(ci.getQuantity())
                    .unitPrice(unit)
                    .lineTotal(lineTotal)
                    .productNameSnapshot(p.getName())
                    .skuSnapshot(p.getSku())
                    .build();

            order.getItems().add(oi);
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);

        // Clear cart
        cart.getItems().clear();

        var savedOrder = orderRepository.save(order);

        // Create payment record
        var payment = Payment.builder()
                .order(savedOrder)
                .provider(PaymentProvider.PAYSTACK)
                .status(PaymentStatus.PENDING)
                .reference("PAY-" + UUID.randomUUID())
                .amount(savedOrder.getTotalAmount())
                .build();

        paymentRepository.save(payment);
        savedOrder.setPayment(payment);

        return savedOrder;
    }
}
