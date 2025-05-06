package org.example.usermanagement.bootstrap;


import org.example.usermanagement.model.RoleName;
import org.example.usermanagement.model.User;
import org.example.usermanagement.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userService.existsByEmail("admin@example.com")) return;
        userService.save(User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(RoleName.ROLE_ADMIN))
                .active(true)
                .build());
    }
}