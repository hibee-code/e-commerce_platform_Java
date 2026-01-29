package com.example.ecommerce.order.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddress {

    private String fullName;
    private String phone;

    private String addressLine1;
    private String addressLine2;

    private String city;
    private String state;
    private String country;

    private String postalCode;
}