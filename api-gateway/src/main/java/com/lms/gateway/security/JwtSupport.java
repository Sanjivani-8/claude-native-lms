package com.lms.gateway.security;

import com.lms.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * Validates and reads claims from JWTs issued by auth-service. The gateway
 * shares the same HMAC secret as auth-service (see application.yml `jwt.secret`
 * in both services) so it can verify a token's signature at the perimeter
 * without calling back into auth-service on every request.
 */
@Component
public class JwtSupport {

    private final Key signingKey;

    public JwtSupport(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parses and validates the token's signature and expiration.
     * @return the claims if valid, or {@code null} if the token is missing, malformed,
     *         expired, or fails signature verification.
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
