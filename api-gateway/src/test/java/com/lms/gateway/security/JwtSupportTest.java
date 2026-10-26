package com.lms.gateway.security;

import com.lms.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtSupportTest {

    private static final String SECRET = "ChangeThisSecretKeyForLMSProjectJWTSigningMinimum256BitsLong!";

    private JwtProperties propertiesWith(String secret) {
        JwtProperties props = new JwtProperties();
        props.setSecret(secret);
        return props;
    }

    private String tokenSignedWith(String secret, long expiresInMs) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 42L);
        claims.put("role", "STUDENT");
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject("jdoe")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiresInMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void parseClaims_validToken_returnsClaims() {
        JwtSupport jwtSupport = new JwtSupport(propertiesWith(SECRET));
        String token = tokenSignedWith(SECRET, 60_000);

        Claims claims = jwtSupport.parseClaims(token);

        assertEquals("jdoe", claims.getSubject());
        assertEquals("STUDENT", claims.get("role", String.class));
        assertEquals(42, ((Number) claims.get("userId")).intValue());
    }

    @Test
    void parseClaims_expiredToken_returnsNull() {
        JwtSupport jwtSupport = new JwtSupport(propertiesWith(SECRET));
        String token = tokenSignedWith(SECRET, -1_000); // already expired

        assertNull(jwtSupport.parseClaims(token));
    }

    @Test
    void parseClaims_wrongSigningKey_returnsNull() {
        JwtSupport jwtSupport = new JwtSupport(propertiesWith(SECRET));
        String token = tokenSignedWith("SomeOtherSecretKeyThatDoesNotMatchTheGatewayConfig!", 60_000);

        assertNull(jwtSupport.parseClaims(token));
    }

    @Test
    void parseClaims_garbageToken_returnsNull() {
        JwtSupport jwtSupport = new JwtSupport(propertiesWith(SECRET));

        assertNull(jwtSupport.parseClaims("not-a-real-token"));
    }
}
