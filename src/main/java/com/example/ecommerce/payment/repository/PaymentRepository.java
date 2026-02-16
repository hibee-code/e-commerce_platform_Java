package com.example.ecommerce.payment.repository;

import com.example.ecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.order o
        WHERE p.reference = :reference
    """)
    Optional<Payment> findByReference(@Param("reference") String reference);
}
