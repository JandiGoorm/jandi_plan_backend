package com.jandi.plan_backend.config;

import com.jandi.plan_backend.user.security.CustomUserDetailsService;
import com.jandi.plan_backend.user.security.JwtAuthenticationFilter;
import com.jandi.plan_backend.user.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/*
 * Spring Security 관련 설정을 담당하는 클래스.
 * JWT를 사용한 인증 방식과 stateless 세션 정책을 적용함.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // JWT 토큰 생성/검증 담당 객체
    private final JwtTokenProvider jwtTokenProvider;
    // 사용자 정보를 DB에서 로드하는 서비스
    private final CustomUserDetailsService customUserDetailsService;

    /*
     * 생성자에서 JwtTokenProvider와 CustomUserDetailsService를 주입받음.
     * 이 두 객체는 JWT 기반 인증 처리에 필요함.
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /*
     * HTTP 보안 설정을 담당하는 SecurityFilterChain 빈 정의.
     * - 세션을 사용하지 않고, 모든 요청에 대해 JWT 토큰을 통한 인증을 적용함.
     * - 로그인 및 회원가입 요청은 인증 없이 접근 허용.
     * - JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 배치함.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 세션을 사용하지 않도록 설정 (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF 보호 비활성화 (API 서버에서는 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 없이 접근 가능한 엔드 포인트
                        // -> 로그인, 회원 가입, 게시판 관련, 공지 사항, 배너
                        .requestMatchers(
                                "/api/users/login", "/api/users/register",
                                "/api/notice/lists",
                                "/api/images/url",
                                "api/community/*",
                                "/api/banner/lists"
                        ).permitAll()
                        // 그 외의 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        // 구성한 보안 설정을 빌드해서 반환
        return http.build();
    }

    /*
     * 인증 관리자를 빈으로 등록.
     * AuthenticationConfiguration을 이용해 AuthenticationManager를 가져옴.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /*
     * 비밀번호 암호화를 위해 BCryptPasswordEncoder를 빈으로 등록.
     * 회원가입이나 로그인 시 비밀번호 비교에 사용됨.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
