package com.myspot.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Authentication authentication) {
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        return createToken(userPrincipal.getEmail(), userPrincipal.getId().toString());
    }

    public String createToken(String email, String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(email)  // Changed from setSubject to subject
                .claim("userId", userId)
                .issuedAt(new Date())  // Changed from setIssuedAt to issuedAt
                .expiration(expiryDate)  // Changed from setExpiration to expiration
                .signWith(getSigningKey(), Jwts.SIG.HS512)  // Changed SignatureAlgorithm to Jwts.SIG
                .compact();
    }

    public String createRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(email)  // Changed from setSubject to subject
                .claim("type", "refresh")
                .issuedAt(new Date())  // Changed from setIssuedAt to issuedAt
                .expiration(expiryDate)  // Changed from setExpiration to expiration
                .signWith(getSigningKey(), Jwts.SIG.HS512)  // Changed SignatureAlgorithm to Jwts.SIG
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()  // Changed from parserBuilder to parser
                .verifyWith(getSigningKey())  // Changed from setSigningKey to verifyWith
                .build()
                .parseSignedClaims(token)  // Changed from parseClaimsJws to parseSignedClaims
                .getPayload();  // Changed from getBody to getPayload

        return claims.getSubject();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()  // Changed from parserBuilder to parser
                .verifyWith(getSigningKey())  // Changed from setSigningKey to verifyWith
                .build()
                .parseSignedClaims(token)  // Changed from parseClaimsJws to parseSignedClaims
                .getPayload();  // Changed from getBody to getPayload

        return claims.get("userId", String.class);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()  // Changed from parserBuilder to parser
                    .verifyWith(getSigningKey())  // Changed from setSigningKey to verifyWith
                    .build()
                    .parseSignedClaims(authToken);  // Changed from parseClaimsJws to parseSignedClaims
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }
}