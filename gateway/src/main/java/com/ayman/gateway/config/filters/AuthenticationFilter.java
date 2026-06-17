package com.ayman.gateway.config.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuthenticationToken -> (Jwt) jwtAuthenticationToken.getPrincipal())
                .map(jwt -> {
                    // Extract Supabase claims
                    String userId = jwt.getSubject(); // The 'sub' claim (User ID in Supabase)

                    // Mutate the request to append custom headers for downstream services
                    return exchange.mutate()
                            .request(builder -> builder
                                            .header("X-User-Id", userId)
                                    // Optional: If you have roles/permissions in your Supabase JWT, extract and add them here too
                                    // .header("X-User-Roles", jwt.getClaimAsStringList("roles").toString())
                            )
                            .build();
                })
                .defaultIfEmpty(exchange) // If request is unauthenticated (like /actuator/**), pass it through as-is
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // High priority order to ensure headers are injected right after Spring Security finishes authentication
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
