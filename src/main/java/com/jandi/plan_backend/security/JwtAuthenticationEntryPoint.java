package com.jandi.plan_backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 인증이 필요한 엔드포인트에서 인증이 되지 않을 시 스프링부트 시큐리티는 403으로 처리
 *  따라서 401로 반환해주기 위해 별도의 응답 폼을 커스텀할 필요가 있었음
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{ \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"로그인이 필요합니다.\" }");
    }
}

