package com.example.ecommerce.auth.dto;

import com.example.ecommerce.user.entity.RoleName;
import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @NotBlank private String fullName;
    @Email @NotBlank private String email;
    @NotBlank
    @Size(min = 8, max = 72)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$",
            message = "Password must be 8-72 chars with upper, lower, number, and special character"
    )
    private String password;
    @NotNull private RoleName role;
}
