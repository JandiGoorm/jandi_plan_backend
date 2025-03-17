package com.jandi.plan_backend.security;

import com.jandi.plan_backend.user.entity.Role;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key secretKey;

    // 액세스 토큰 유효기간 (15분)
    private final long validityInMilliseconds = 15 * 60 * 1000;

    // 리프레시 토큰 유효기간 (7일)
    private final long refreshValidityInMilliseconds = 7 * 24 * 60 * 60 * 1000;
    private final UserRepository userRepository;

    /**
     * application.properties에 설정한 jwt.secret 값을 주입받아 SecretKey를 생성합니다.
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret, UserRepository userRepository) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.userRepository = userRepository;
    }

    /**
     * 이메일을 입력받아 액세스 토큰을 생성합니다.
     *
     * @param email 사용자 이메일
     * @return 생성된 액세스 토큰 문자열
     */
    public String createToken(String email) {
        log.info("이메일 '{}' 에 대해 액세스 JWT 토큰 생성 시작", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);
        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", "ROLE_" + user.getRoleEnum().name())  // 유저의 권한을 jwt에 저장
                //유저가 존재할 때 유저 권환 저장
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
        log.info("토큰 생성 - 사용자 이메일: {}, 역할: {}", email, user.getRoleEnum().name());
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

    public Role getUserRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String roleStr = claims.get("role", String.class);
            log.debug("토큰에서 추출한 권한: {}", roleStr);

            return Role.valueOf(roleStr.replace("ROLE_", ""));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("토큰에서 권한 추출 실패: {}", e.getMessage());
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
        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰 유효성 검사 실패: 토큰 만료");
        } catch (SecurityException e) {
            log.error("JWT 토큰 유효성 검사 실패: 유효하지 않은 서명");
        } catch (MalformedJwtException e) {
            log.error("JWT 토큰 유효성 검사 실패: 잘못된 형식의 토큰");
        } catch (UnsupportedJwtException e) {
            log.error("JWT 토큰 유효성 검사 실패: 지원되지 않는 토큰");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰 유효성 검사 실패: 빈 토큰 또는 잘못된 토큰");
        }
        return false;
    }

}
