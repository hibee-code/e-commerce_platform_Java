package com.example.ecommerce.auth.service;


import com.example.ecommerce.auth.dto.AuthResponse;
import com.example.ecommerce.auth.dto.LoginRequest;
import com.example.ecommerce.auth.dto.RegisterRequest;
import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.common.exception.ConflictException;
import com.example.ecommerce.user.entity.Role;
import com.example.ecommerce.user.entity.RoleName;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repository.RoleRepository;
import com.example.ecommerce.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthAuditLogger auditLogger;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            auditLogger.registerFailure(req.getEmail(), req.getRole(), "email_exists");
            throw new ConflictException("Email already exists");
        }

        RoleName requestedRole = req.getRole();
        var role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> {
                    auditLogger.registerFailure(req.getEmail(), requestedRole, "role_not_found");
                    return new BadRequestException("Role not found");
                });

        var saved = userRepository.save(buildUser(req, role));
        auditLogger.registerSuccess(saved.getId(), saved.getEmail(), role.getName());
        return new AuthResponse(jwtService.generate(saved, role.getName()));
    }

    public AuthResponse login(LoginRequest req) {
        var optional = userRepository.findByEmailWithRoles(req.getEmail());
        if (optional.isEmpty()) {
            auditLogger.loginFailure(req.getEmail(), req.getRole(), "user_not_found");
            throw new BadRequestException("Invalid credentials");
        }
        var user = optional.get();

        if (!user.isEnabled()) {
            auditLogger.loginFailure(req.getEmail(), req.getRole(), "disabled");
            throw new BadRequestException("Invalid credentials");
        }

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            auditLogger.loginFailure(req.getEmail(), req.getRole(), "bad_password");
            throw new BadRequestException("Invalid credentials");
        }

        RoleName role = req.getRole();
        boolean hasRole = user.getRoles().stream().anyMatch(r -> r.getName() == role);
        if (!hasRole) {
            auditLogger.loginFailure(req.getEmail(), role, "role_not_assigned");
            throw new BadRequestException("Role not assigned");
        }

        auditLogger.loginSuccess(user.getId(), user.getEmail(), role);
        return new AuthResponse(jwtService.generate(user, role));
    }

    @Transactional
    public AuthResponse registerFirstAdmin(RegisterRequest req) {
        if (req.getRole() != RoleName.ROLE_ADMIN) {
            auditLogger.setupAdminFailure(req.getEmail(), "role_not_admin");
            throw new BadRequestException("Role must be ROLE_ADMIN for setup");
        }
        if (userRepository.existsByRole(RoleName.ROLE_ADMIN)) {
            auditLogger.setupAdminFailure(req.getEmail(), "admin_exists");
            throw new ConflictException("Admin already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            auditLogger.setupAdminFailure(req.getEmail(), "email_exists");
            throw new ConflictException("Email already exists");
        }

        var role = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> {
                    auditLogger.setupAdminFailure(req.getEmail(), "role_not_found");
                    return new BadRequestException("Role not found");
                });

        var saved = userRepository.save(buildUser(req, role));
        auditLogger.setupAdminSuccess(saved.getId(), saved.getEmail());
        return new AuthResponse(jwtService.generate(saved, role.getName()));
    }

    private User buildUser(RegisterRequest req, Role role) {
        var user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .passwordHash(encoder.encode(req.getPassword()))
                .enabled(true)
                .build();
        user.getRoles().add(role);
        return user;
    }
}
