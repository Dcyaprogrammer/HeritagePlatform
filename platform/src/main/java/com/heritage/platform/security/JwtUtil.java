package com.heritage.platform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final String secretKey;
    private final SecretKey key;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException("JWT密钥长度必须至少32字节，当前长度: " + (secretKey != null ? secretKey.length() : 0));
        }
        this.secretKey = secretKey;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String username, Set<String> roles, String jti) {        //jti
        Map<String, Object> claims = new HashMap<>();
        String rolesString = roles.stream().collect(Collectors.joining(","));
        claims.put("roles", rolesString);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setId(jti)       //JWTID
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //多会化为管理
    public String extractJti(String token) {
        return extractAllClaims(token).getId();
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