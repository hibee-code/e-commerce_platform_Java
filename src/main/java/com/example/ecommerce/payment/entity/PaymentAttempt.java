package com.example.ecommerce.payment.entity;

import com.example.ecommerce.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_attempts",
        indexes = {
                @Index(name = "idx_payment_attempts_payment", columnList = "payment_id"),
                @Index(name = "idx_payment_attempts_reference", columnList = "reference")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_attempts_payment"))
    private Payment payment;

    @Column(nullable = false, length = 80)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 500)
    private String message;
}
