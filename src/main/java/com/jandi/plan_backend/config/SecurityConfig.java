package com.jandi.plan_backend.config;

// Spring의 설정 클래스로 인식하기 위해 사용함. 해당 클래스를 스캔하여 Bean을 생성함.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// HttpSecurity 설정에 필요한 클래스들
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// 비밀번호 암호화를 위한 클래스들
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // 이 클래스는 스프링의 설정 클래스로 동작하며, Bean 등록 등의 설정을 담당함.
public class SecurityConfig {

    /**
     * PasswordEncoder Bean 등록
     *
     * BCryptPasswordEncoder는 비밀번호를 안전하게 암호화하는 기능을 제공한다.
     * - 비밀번호를 해싱하는 단방향 암호화 기법을 사용함.
     * - 매번 다른 salt 값을 생성하여 동일한 비밀번호라도 해시값이 다르게 만들어, 보안성을 높임.
     * - 다른 컴포넌트(예: UserService)에서 비밀번호 암호화 및 검증 시 주입받아 사용함.
     *
     * @return BCryptPasswordEncoder 인스턴스를 반환하여 PasswordEncoder로 등록함.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChain Bean 등록 및 HTTP 보안 설정 구성
     *
     * HttpSecurity 객체를 사용하여 HTTP 요청에 대한 보안 정책을 정의함.
     * 아래 설정은 다음과 같은 역할을 수행함:
     *  1. CSRF 보호 기능 비활성화
     *     - CSRF(Cross Site Request Forgery) 공격 방지를 위한 기본 기능이나,
     *       API 서버나 RESTful 서비스 등에서는 필요하지 않을 수 있으므로 비활성화함.
     *
     *  2. 요청 권한 설정
     *     - "/api/**"로 시작하는 경로는 인증 없이 접근을 허용함.
     *     - 그 외의 모든 요청은 인증이 필요하도록 설정함.
     *
     *  3. HTTP Basic 인증 활성화
     *     - 브라우저 기본 인증 창을 통해 사용자 인증을 처리함.
     *     - Customizer.withDefaults()를 통해 기본 설정값을 사용함.
     *
     * @param http HttpSecurity 객체를 통해 보안 설정을 구성할 수 있음.
     * @return 구성된 SecurityFilterChain 객체를 반환하여 스프링 시큐리티에서 사용함.
     * @throws Exception 설정 과정에서 발생할 수 있는 예외를 던짐.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 기능 비활성화
                // 웹 브라우저 기반의 폼 인증 등에서는 필요하지만, REST API 서버의 경우 필요하지 않을 수 있음.
                .csrf(csrf -> csrf.disable())

                // HTTP 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // "/api/**" 경로로 시작하는 요청은 모두 인증 없이 접근을 허용함.
                        .requestMatchers("/api/**").permitAll()
                        // 그 외의 모든 요청은 인증을 요구함.
                        .anyRequest().authenticated())

                // HTTP Basic 인증 사용 설정
                // 기본적으로 브라우저의 인증 다이얼로그를 통해 사용자 이름과 비밀번호를 입력받음.
                .httpBasic(Customizer.withDefaults());

        // 설정한 HttpSecurity 객체를 기반으로 SecurityFilterChain을 생성하여 반환함.
        return http.build();
    }
}
