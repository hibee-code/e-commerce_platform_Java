package com.example.ecommerce.user.entity;

import jakarta.persistence.*;
import lombok.*;
import com.example.ecommerce.common.jpa.BaseEntity;

@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoleName name;
}