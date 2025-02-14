package com.jandi.plan_backend.user.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * JwtAuthenticationFilter는 각 HTTP 요청마다 실행되는 필터로,
 * 요청 헤더에서 JWT 토큰을 추출하고, 토큰의 유효성을 검사한 후,
 * 유효한 토큰일 경우 사용자 인증 정보를 SecurityContext에 설정한다.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 토큰 관련 기능(검증, 정보 추출 등)을 제공하는 클래스
    private final JwtTokenProvider jwtTokenProvider;
    // 사용자 정보를 로드하기 위한 UserDetailsService 구현체
    private final UserDetailsService userDetailsService;

    /**
     * 생성자.
     * @param jwtTokenProvider JWT 관련 기능을 제공하는 객체
     * @param userDetailsService 사용자 정보를 가져오는 서비스 객체
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * HTTP 요청마다 호출되는 메서드.
     * 요청 헤더의 Authorization 값을 확인해서 JWT 토큰이 존재하면
     * 토큰을 검증하고, 해당 토큰에 포함된 이메일을 기반으로 사용자 정보를 조회한 후,
     * SecurityContext에 인증 정보를 설정한다.
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인 객체
     * @throws ServletException 서블릿 관련 예외 발생 시 던짐
     * @throws IOException I/O 관련 예외 발생 시 던짐
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // "Authorization" 헤더 값 추출
        String header = request.getHeader("Authorization");

        // 헤더가 null이 아니고 "Bearer "로 시작하면 JWT 토큰이 존재함
        if (header != null && header.startsWith("Bearer ")) {
            // "Bearer " 이후의 문자열을 토큰으로 간주
            String token = header.substring(7);
            log.debug("요청 헤더에서 JWT 토큰 발견");

            // 토큰 유효성 검사
            if (jwtTokenProvider.validateToken(token)) {
                // 토큰에서 이메일 정보 추출
                String email = jwtTokenProvider.getEmail(token);
                log.debug("토큰이 유효함. 이메일: {}", email);

                // 이메일을 기반으로 사용자 정보 조회
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 사용자 정보와 권한을 담아 인증 토큰 생성
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 요청의 세부 정보를 인증 토큰에 설정
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 정보를 설정하여 인증된 상태로 만듦
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // 토큰이 유효하지 않을 경우 경고 로그 출력
                log.warn("유효하지 않은 JWT 토큰");
            }
        } else {
            // Authorization 헤더가 없거나 Bearer 토큰 형식이 아니면 디버그 로그 출력
            log.debug("요청 헤더에 JWT 토큰이 없음");
        }

        // 다음 필터로 요청과 응답 전달
        filterChain.doFilter(request, response);
    }
}
