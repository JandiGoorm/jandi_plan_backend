package com.jandi.plan_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰을 생성하고 검증하는 클래스.
 * HS256 알고리즘을 사용해 토큰에 서명한다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // JWT 서명을 위한 비밀키 (운영 환경에서는 안전하게 관리해야 함)
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 액세스 토큰 유효기간 (15분)
    private final long validityInMilliseconds = 15 * 60 * 1000;

    // 리프레시 토큰 유효기간 (7일)
    private final long refreshValidityInMilliseconds = 7 * 24 * 60 * 60 * 1000;

    /**
     * 이메일을 입력받아 액세스 토큰을 생성한다.
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
     * 이메일을 입력받아 리프레시 토큰을 생성한다.
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
     * JWT 토큰에서 이메일(Subject)을 추출한다.
     *
     * @param token JWT 토큰
     * @return 토큰에 저장된 이메일
     * @throws JwtException 토큰 파싱 중 에러 발생 시
     * @throws IllegalArgumentException 토큰이 잘못되었을 때
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
            return null; //예외를 그대로 반환하면 400 에러가 될 가능성이 크므로, null을 반환시켜 에러를 숨기고 인증을 진행하여 401 에러가 나도록 함
        }
    }

    /**
     * JWT 토큰의 유효성을 검사한다.
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
