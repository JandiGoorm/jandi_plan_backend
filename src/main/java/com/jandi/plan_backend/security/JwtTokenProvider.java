package com.jandi.plan_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key secretKey;

    // 액세스 토큰 유효기간 (15분)
    private final long validityInMilliseconds = 15 * 60 * 1000;

    // 리프레시 토큰 유효기간 (7일)
    private final long refreshValidityInMilliseconds = 7 * 24 * 60 * 60 * 1000;

    /**
     * application.properties에 설정한 jwt.secret 값을 주입받아 SecretKey를 생성합니다.
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 이메일을 입력받아 액세스 토큰을 생성합니다.
     *
     * @param email 사용자 이메일
     * @return 생성된 액세스 토큰 문자열
     */
    public String createToken(String email) {
        log.info("이메일 '{}' 에 대해 액세스 JWT 토큰 생성 시작", email);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
        log.info("액세스 토큰 생성 완료. 만료 시간: {}", expiry);
        return token;
    }

    /**
     * 이메일을 입력받아 리프레시 토큰을 생성합니다.
     *
     * @param email 사용자 이메일
     * @return 생성된 리프레시 토큰 문자열
     */
    public String createRefreshToken(String email) {
        log.info("이메일 '{}' 에 대해 리프레시 JWT 토큰 생성 시작", email);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshValidityInMilliseconds);
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
        log.info("리프레시 토큰 생성 완료. 만료 시간: {}", expiry);
        return token;
    }

    /**
     * JWT 토큰에서 이메일(Subject)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에 저장된 이메일
     */
    public String getEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("토큰에서 추출한 이메일: {}", claims.getSubject());
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JWT 토큰의 유효성을 검사합니다.
     *
     * @param token 검사할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            log.debug("JWT 토큰 유효함");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }
}
