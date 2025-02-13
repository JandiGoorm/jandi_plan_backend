package com.jandi.plan_backend.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    // 비밀키 (운영 환경에서는 안전한 방식으로 관리)
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 액세스 토큰 유효기간: 15분
    private final long validityInMilliseconds = 15 * 60 * 1000;

    public String createToken(String email) {
        log.info("Creating JWT token for email: {}", email);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
        log.info("JWT token created, expires at {}", expiry);
        return token;
    }

    public String getEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("Extracted email '{}' from token", claims.getSubject());
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract email from token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            log.debug("JWT token is valid");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
}
