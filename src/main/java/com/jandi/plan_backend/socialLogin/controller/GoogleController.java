package com.jandi.plan_backend.socialLogin.controller;

import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.socialLogin.service.GoogleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GoogleController {

    private final GoogleService googleService;

    /**
     * 1) 실제 구글 로그인 로직
     *    프론트에서 GET /auth/googleLogin?code=... 로 요청
     */
    @GetMapping("/googleLogin")
    public ResponseEntity<?> googleLogin(@RequestParam(required = false) String code) {
        // code가 없으면 에러
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("No code provided");
        }

        // 구글 OAuth 로그인 처리 (토큰 -> 사용자 정보 -> DB 가입/조회 -> JWT 발급)
        AuthRespDTO authResp;
        try {
            authResp = googleService.googleLogin(code);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("구글 로그인 처리 실패: " + e.getMessage());
        }

        // 최종적으로 JWT(액세스 토큰, 리프레시 토큰) 반환
        return ResponseEntity.ok(authResp);
    }

    /**
     * 2) 콜백 전용 엔드포인트
     *    단순히 code 파라미터를 JSON으로 보여주는 간단한 로직 예시
     *    예: GET /api/auth/googleLogin/callback?code=...
     */
    @GetMapping("/googleLogin/callback")
    public ResponseEntity<?> googleLoginCallback(@RequestParam(required = false) String code) {
        // 이 엔드포인트에서는 실제 로그인 처리를 하지 않고,
        // code 파라미터만 확인하여 JSON으로 반환하는 예시
        return ResponseEntity.ok(Map.of(
                "message", "구글 로그인 callback 엔드포인트입니다.",
                "code", code
        ));
    }
}
