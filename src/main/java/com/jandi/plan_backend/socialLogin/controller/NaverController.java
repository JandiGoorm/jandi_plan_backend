package com.jandi.plan_backend.socialLogin.controller;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.socialLogin.dto.NaverUserInfo;
import com.jandi.plan_backend.socialLogin.service.NaverService;
import com.jandi.plan_backend.user.dto.AuthRespDTO;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth/naver")
@RequiredArgsConstructor
public class NaverController {

    private final NaverService naverService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/loginUrl")
    public ResponseEntity<String> getLoginUrl() {
        // 상태 토큰으로 사용할 랜덤 문자열 생성
        // 구글 클라우드 런은 세션 저장이 되지 않기 때문에 state를 jwt 생성하여 저장
        String state = jwtTokenProvider.createStateToken(); // 세션 대신 JWT 생성
        log.info("state: {}", state);
        String loginUrl = naverService.createLoginUrl(state);
        return ResponseEntity.ok(loginUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        // CSRF 방지를 위해 state의 유효성을 확인하여 state가 위조되지 않았는지 검증
        if (!jwtTokenProvider.validateStateToken(state)) {
            return ResponseEntity.badRequest().body("state 토큰이 유효하지 않습니다: " + state);
        }
        // 토큰 검증 성공 시 액세스 토큰 받아서 유저 정보를 얻어옴
        String accessToken = naverService.getAccessToken(code, state);
        log.debug("accessToken: {}", accessToken);
        NaverUserInfo userInfo = naverService.getUserInfo(accessToken);
        log.debug("userInfo: {}", userInfo);

        // 로그인 처리
        AuthRespDTO authResp = naverService.naverLogin(userInfo);
        // 로그인이 완료되면 네이버의 액세스 토큰은 삭제
        naverService.deleteAccessToken(accessToken);
        return ResponseEntity.ok(authResp);
    }
}

