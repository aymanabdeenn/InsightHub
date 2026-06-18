package com.ayman.gateway.config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.cloud.gateway.server.mvc.filter.FilterSupplier;
import org.springframework.cloud.gateway.server.mvc.filter.SimpleFilterSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayFilterConfig {

    @Bean
    FilterSupplier userGatewayFilterSupplier() {
        return new SimpleFilterSupplier(UserGatewayFilterFunctions.class);
    }

    public static final class UserGatewayFilterFunctions {

        private static final Map<String, Bucket> BUCKETS = new ConcurrentHashMap<>();

        private UserGatewayFilterFunctions() {
        }

        public static HandlerFilterFunction<ServerResponse, ServerResponse> inMemoryRateLimit(
                long capacity,
                Duration period
        ) {
            return (request, next) -> {
                String key = currentUserSub();
                Bucket bucket = BUCKETS.computeIfAbsent(key, ignored -> Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(capacity)
                                .refillGreedy(capacity, period)
                                .build())
                        .build());

                if (!bucket.tryConsume(1)) {
                    return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                            .header("X-RateLimit-Remaining", "0")
                            .build();
                }

                ServerResponse response = next.handle(request);
                response.headers().set("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
                return response;
            };
        }

        public static HandlerFilterFunction<ServerResponse, ServerResponse> addCurrentUserHeader(String name) {
            return (request, next) -> next.handle(ServerRequest.from(request)
                    .headers(headers -> headers.set(name, currentUserSub()))
                    .build());
        }

        private static String currentUserSub() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
                return jwtAuthentication.getToken().getSubject();
            }
            if (authentication != null) {
                return authentication.getName();
            }
            return "anonymous";
        }
    }
}
