package com.jandi.plan_backend.user.security;

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

    // JWT 서명을 위한 비밀키
    // 운영 환경에서는 이 키를 안전하게 관리해야 함
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 액세스 토큰 유효기간 (15분)
    private final long validityInMilliseconds = 15 * 60 * 1000;

    /**
     * 이메일을 입력받아 JWT 토큰을 생성한다.
     *
     * 1. 현재 시간을 기준으로 만료 시간을 계산.
     * 2. 토큰의 subject에 이메일을 저장.
     * 3. 발급 시간과 만료 시간을 설정.
     * 4. secretKey로 토큰에 서명.
     *
     * @param email 사용자 이메일
     * @return 생성된 JWT 토큰 문자열
     */
    public String createToken(String email) {
        log.info("이메일 '{}' 에 대해 JWT 토큰 생성 시작", email);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);

        String token = Jwts.builder()
                .setSubject(email)         // 토큰의 주체(subject)를 이메일로 설정
                .setIssuedAt(now)          // 토큰 발급 시간 설정
                .setExpiration(expiry)     // 토큰 만료 시간 설정
                .signWith(secretKey)       // secretKey로 토큰에 서명
                .compact();                // 토큰 문자열로 압축

        log.info("JWT 토큰 생성 완료. 만료 시간: {}", expiry);
        return token;
    }

    /**
     * JWT 토큰에서 이메일(Subject)을 추출한다.
     *
     * 1. secretKey를 사용해 토큰을 파싱 및 검증.
     * 2. 클레임에서 subject 값을 반환.
     *
     * @param token JWT 토큰
     * @return 토큰에 저장된 이메일
     * @throws JwtException 토큰 파싱 중 에러 발생 시 던짐
     * @throws IllegalArgumentException 토큰이 잘못되었을 때 던짐
     */
    public String getEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)    // 토큰 서명 검증에 사용
                    .build()
                    .parseClaimsJws(token)       // 토큰 파싱 및 검증
                    .getBody();                  // 클레임(토큰 본문) 가져오기
            log.debug("토큰에서 추출한 이메일: {}", claims.getSubject());
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * JWT 토큰의 유효성을 검사한다.
     *
     * 1. secretKey를 사용해 토큰을 파싱해 서명을 확인.
     * 2. 토큰이 만료되었거나 잘못된 경우 예외가 발생함.
     *
     * @param token 검사할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)  // 토큰 서명 검증에 사용
                    .build()
                    .parseClaimsJws(token);    // 토큰 파싱 시도 (검증 수행)
            log.debug("JWT 토큰 유효함");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }
}
