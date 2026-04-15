package com.heritage.platform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "HeritagePlatform2026SecretKeyForJWTAtLeast32BytesLong!!!";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(String username, Set<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        String rolesString = roles.stream().collect(Collectors.joining(","));
        claims.put("roles", rolesString);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        String rolesString = extractAllClaims(token).get("roles", String.class);
        if (rolesString == null || rolesString.isEmpty()) {
            return null;
        }
        return rolesString;
    }

    public Set<String> extractRoles(String token) {
        String rolesString = extractAllClaims(token).get("roles", String.class);
        if (rolesString == null || rolesString.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(rolesString.split(",")));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}