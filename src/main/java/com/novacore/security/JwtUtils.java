package com.novacore.security;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.access-token-expiration}") private long accessExpiry;
    @Value("${jwt.refresh-token-expiration}") private long refreshExpiry;

    private SecretKey key() { return Keys.hmacShaKeyFor(secret.getBytes()); }

    public String generateAccess(String email, Map<String, Object> claims) {
        return Jwts.builder().claims(claims).subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessExpiry))
            .id(UUID.randomUUID().toString())
            .signWith(key()).compact();
    }

    public String generateRefresh(String email) {
        return Jwts.builder().subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpiry))
            .id(UUID.randomUUID().toString())
            .signWith(key()).compact();
    }

    public String extractEmail(String token) { return extract(token, Claims::getSubject); }
    public boolean isValid(String token) {
        try { return !extract(token, Claims::getExpiration).before(new Date()); }
        catch (Exception e) { return false; }
    }
    private <T> T extract(String token, Function<Claims, T> fn) {
        return fn.apply(Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload());
    }
    public long getAccessExpiry() { return accessExpiry; }
}
