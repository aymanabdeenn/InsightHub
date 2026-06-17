package com.ayman.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Supplier;

@Configuration
public class RateLimiterConfig {

    @Bean
    public Supplier<String> currentUserSub() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return (auth != null) ? auth.getName() : "anonymous";
        };
    }

}
