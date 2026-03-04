package com.example.ecommerce.user.service;

import com.example.ecommerce.user.entity.Role;
import com.example.ecommerce.user.entity.RoleName;
import com.example.ecommerce.user.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed(RoleName.ROLE_USER);
        seed(RoleName.ROLE_ADMIN);
    }

    private void seed(RoleName name) {
        if (roleRepository.findByName(name).isPresent()) {
            return;
        }
        roleRepository.save(Role.builder().name(name).build());
    }
}
