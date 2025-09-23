package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;


@Configuration
public class AdminSetupConfig {

    @Bean
    public CommandLineRunner createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@example.com";

            // Check if admin already exists
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123")); // encode password
                admin.setRole(UserRole.ADMIN);
                admin.setEnabled(true);

                userRepository.save(admin);
                System.out.println("✅ Admin user created: " + adminEmail + " / admin123");
            } else {
                System.out.println("⚠️ Admin user already exists: " + adminEmail);
            }
        };
    }
}
