package com.jandi.plan_backend.config;

import com.jandi.plan_backend.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    /**
     * 생성자 주입: JWT 토큰 생성/검증과 사용자 정보 로드를 위한 CustomUserDetailsService를 주입받습니다.
     */
    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    /**
     * CORS 설정을 위한 CorsConfigurationSource 빈.
     * - 모든 HTTP 메서드와 헤더를 허용하며, 쿠키와 인증 정보를 포함한 요청도 허용합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // 필요 시 구체적으로 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
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
                        /** 미로그인 상태에서도 접근 가능 */
                        .requestMatchers(
                                // user - login & register 관련
                                "/api/users/login", "/api/users/register", "/api/users/register/checkEmail",
                                "/api/users/register/checkName", "/api/users/forgot", "/api/users/verify",
                                "/api/users/token/refresh"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                // community - post 관련
                                "/api/community/posts", "/api/community/posts/{postId}", "/api/community/search",

                                // community - comment 관련
                                "/api/community/comments/{postId}", "/api/community/replies/{commentId}",

                                // resource - notice & banner 관련
                                "/api/banner/lists", "/api/notice/lists",

                                // trip 관련
                                "/api/trip/*", "/api/trip/itinerary/*", "/api/trip/reservation/*",

                                "/error"
                        ).permitAll()

                        /** 로그인 상태에서만 접근 가능 */
                        // 일반 사용자 이상의 권한 필요
                        .requestMatchers(
                                // image 관련
                                "/api/images/**",

                                // users 관련
                                "api/users/del-user", "api/users/change-password", "/api/users/profile",

                                // users - prefer 관련
                                "api/trip/cities/prefer",

                                // community - post 관련
                                "api/community/posts", "/api/community/posts/{postId}", "/api/temp",
                                "/api/community/posts/reports{postId}", "/api/community/posts/likes/{postId}",

                                // community - comments 관련
                                "/api/community/replies/{commentId}", "/api/community/comments/{postId}", "/api/community/comments/{commentId}",
                                "/api/community/comments/reports/{commentId}", "/api/community/comments/likes/{commentId}",

                                // trip - main 관련
                                "/api/trip/my/allTrips", "/api/trip/my/{tripId}",
                                "/api/trip/my/likedTrips", "/api/trip/my/likedTrips/{tripId}",

                                // trip - participant 관련
                                "/api/trip/{tripId}/participants", "/api/trip/{tripId}/participants/{participantUserName}",

                                // trip - detail 관련
                                "/api/trip/my/create",
                                "/api/trip/reservation/{tripId}", "/api/trip/reservation/{ReservationId}",
                                "/api/trip/itinerary/{tripId}", "/api/trip/itinerary/{itineraryId}",
                                "/api/place", "/api/place/{placeId}", "/api/place/",

                                // city - recommend 관련
                                "/api/map/recommend/restaurant"
                        ).hasAnyRole("USER", "STAFF","ADMIN")

                        // 스텝 이상의 권한 필요
                        .requestMatchers(
                                // 유저 제한
                                "/api/manage/user/permit/{userId}",

                                // 목록 조회
                                "/api/manage/user/all", "/api/manage/user/reported",
                                "/api/manage/community/reported/comments", "/api/manage/community/reported/posts",

                                // 통계 조회
                                "/api/manage/util/all", "/api/manage/util/month/users"
                        ).hasAnyRole("STAFF", "ADMIN")

                        // 관리자 권한 필요
                        .requestMatchers(
                                // resource - notice & banner 관련
                                "/api/notice/", "/api/notice/{noticeId}",
                                "/api/banner/lists", "/api/banner/lists/{bannderId}", "/api/images/upload/notice",

                                // trip - tripData 관련
                                "/api/manage/trip/continents",
                                "/api/manage/trip/countries", "/api/manage/trip/countries/{countryId}",
                                "/api/manage/trip/cities", "/api/manage/trip/cities/{cityId}",

                                // reported - delete 관련
                                "/api/manage/user/delete/{userId}",
                                "/api/manage/community/delete/posts/{postId}",
                                "/api/manage/community/delete/comments/{commentId}",

                                // 유저 관련
                                "api/manage/user/change-role/{user_id}"
                        ).hasRole("ADMIN")
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 미로그인 시 401 반환
                        .accessDeniedHandler(jwtAccessDeniedHandler) // 권한없을 시 403 반환
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
