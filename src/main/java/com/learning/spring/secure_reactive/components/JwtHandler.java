package com.learning.spring.secure_reactive.components;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtHandler {

    @Value("${jwt.client-secret:defaultKey}")
    private String jwtSecret;

    public String generateJwtToken(String subject) {
        return Jwts.builder()
                .claim("sub", subject)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(getKey())
                .compact();
    }

    public Mono<Boolean> validateJwt(String token) {
        return Mono.just(token)
                .map(this::parseToken)
                .map(claims -> !claims.getExpiration().before(new Date()))
                .onErrorReturn(false);
    }

    public String extractTokenSubject(String token){
        return parseToken(token).getSubject();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Optional.ofNullable(jwtSecret)
                .map(String::getBytes)
                .map(Keys::hmacShaKeyFor)
                .orElseThrow(() -> new IllegalArgumentException("token secret must be configured in the application properties"));
    }
}
