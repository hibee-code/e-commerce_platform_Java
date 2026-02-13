package com.example.ecommerce.Auth.dto;

import com.example.ecommerce.user.entity.RoleName;
import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class LoginRequest {
    @Email @NotBlank private String email;
    @NotBlank private String password;
    @NotNull private RoleName role;
}
