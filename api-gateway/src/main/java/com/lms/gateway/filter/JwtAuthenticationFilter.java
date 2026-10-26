package com.lms.gateway.filter;

import com.lms.gateway.config.JwtProperties;
import com.lms.gateway.security.JwtSupport;
import io.jsonwebtoken.Claims;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Perimeter authentication for the gateway. Every request not matching one of
 * `jwt.public-paths` must carry a valid `Authorization: Bearer <token>` header
 * signed by auth-service. On success, the caller's identity is propagated
 * downstream via `X-User-Id` / `X-Username` / `X-User-Role` headers so backend
 * services can trust them without re-validating the JWT themselves.
 *
 * Runs before Spring Cloud Gateway's routing filter (see {@link Ordered}).
 */
@Component
@Order(-1)
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtProperties jwtProperties;
    private final JwtSupport jwtSupport;

    public JwtAuthenticationFilter(JwtProperties jwtProperties, JwtSupport jwtSupport) {
        this.jwtProperties = jwtProperties;
        this.jwtSupport = jwtSupport;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtSupport.parseClaims(token);
        if (claims == null) {
            return reject(exchange, "Invalid or expired token");
        }

        Object userId = claims.get("userId");
        Object role = claims.get("role");

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Username", claims.getSubject() == null ? "" : claims.getSubject())
                .header("X-User-Id", userId == null ? "" : userId.toString())
                .header("X-User-Role", role == null ? "" : role.toString())
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return jwtProperties.getPublicPaths().stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        return response.writeWith(Mono.just(response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
