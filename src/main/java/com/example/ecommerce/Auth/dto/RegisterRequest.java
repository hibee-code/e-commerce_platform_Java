package com.example.ecommerce.Auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @NotBlank private String fullName;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;
}
