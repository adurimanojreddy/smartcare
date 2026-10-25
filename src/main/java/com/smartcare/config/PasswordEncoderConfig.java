package com.smartcare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password hashing bean (Module 1 - "Password encryption using BCrypt").
 * Kept separate from the full Spring Security filter chain (SecurityConfig,
 * added in Phase 7) so the service layer can depend on PasswordEncoder
 * without waiting on the rest of the security wiring.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
