package com.jandi.plan_backend.config;

import com.jandi.plan_backend.security.CustomUserDetailsService;
import com.jandi.plan_backend.security.JwtAuthenticationFilter;
import com.jandi.plan_backend.security.JwtTokenProvider;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * 생성자 주입: JWT 토큰 생성/검증과 사용자 정보 로드를 위한 CustomUserDetailsService를 주입받습니다.
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * CORS 설정을 위한 CorsConfigurationSource 빈.
     * - 모든 HTTP 메서드와 헤더를 허용하며, 쿠키와 인증 정보를 포함한 요청도 허용합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // 필요 시 구체적으로 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // 쿠키 및 인증 정보 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * HTTP 보안 설정을 담당하는 SecurityFilterChain 빈.
     * - lambda 스타일로 CORS 설정을 적용하고, stateless 세션 정책 및 CSRF 비활성화를 적용합니다.
     * - 특정 엔드포인트에 대해 인증 없이 접근을 허용하고, 나머지 요청은 인증을 요구합니다.
     * - JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가합니다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // lambda 스타일로 커스텀 CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션을 사용하지 않도록 설정 (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF 보호 비활성화 (API 서버에서는 필요 없음)
                .csrf(AbstractHttpConfigurer::disable)
                // 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/users/login", "/api/users/register", "/api/users/forgot",
                                "/api/notice/lists",
                                "api/community/posts", "api/community/posts/*",
                                "api/community/comments", "api/community/comments/{postId}",
                                "api/community/replies/{commentId}", "api/community/posts", 
                                "/api/images/**",
                                "/api/banner/lists"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 인증 관리자(AuthenticationManager)를 빈으로 등록.
     * AuthenticationConfiguration을 통해 자동으로 구성된 AuthenticationManager를 반환합니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * BCryptPasswordEncoder를 빈으로 등록.
     * 회원가입이나 로그인 시 비밀번호 암호화 및 비교에 사용됩니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
