package com.jandi.plan_backend.socialLogin.controller;

import com.jandi.plan_backend.socialLogin.dto.NaverUserInfo;
import com.jandi.plan_backend.socialLogin.service.NaverService;
import com.jandi.plan_backend.user.dto.AuthRespDTO;
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

    @GetMapping("/loginUrl")
    public ResponseEntity<String> getLoginUrl(HttpSession session) {
        // 상태 토큰으로 사용할 랜덤 문자열 생성
        // 상태 토큰은 추후 검증을 위해 세션에 저장되어야 한다.
        String state = UUID.randomUUID().toString();
        session.setAttribute("naver_oauth_state", state);
        log.info("state: {}", state);

        String loginUrl = naverService.createLoginUrl(state);
        return ResponseEntity.ok(loginUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session
    ) {
        // 세션 또는 별도의 저장 공간에서 상태 토큰을 가져옴
        String storedState = (String) session.getAttribute("naver_oauth_state");
        log.info("storedState: {}", storedState);

        // CSRF 방지를 위한 상태 토큰 검증 검증
        if (!state.equals(storedState)) {
            return ResponseEntity.badRequest().body("state가 일치하지 않습니다!");
        }

        // 토큰 검증 성공 시 액세스 토큰 받아서 유저 정보를 얻어옴
        String accessToken = naverService.getAccessToken(code, storedState);
        log.info("accessToken: {}", accessToken);
        NaverUserInfo userInfo = naverService.getUserInfo(accessToken);
        log.info("userInfo: {}", userInfo);

        // 로그인 처리
        AuthRespDTO authResp = naverService.naverLogin(userInfo);
        // 로그인이 완료되면 네이버의 액세스 토큰은 삭제
        naverService.deleteAccessToken(accessToken);
        return ResponseEntity.ok(authResp);
    }
}

