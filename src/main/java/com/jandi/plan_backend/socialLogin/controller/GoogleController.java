package com.jandi.plan_backend.socialLogin.controller;

import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.socialLogin.service.GoogleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GoogleController {

    private final GoogleService googleService;

    /**
     * 프론트에서 code를 쿼리 파라미터로 GET 요청
     * 예: GET /api/auth/googleLogin?code=abc123
     */
    @GetMapping("/googleLogin")
    public ResponseEntity<?> googleLogin(@RequestParam(required = false) String code) {
        // 1) code가 없으면 에러
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("No code provided");
        }

        // 2) 구글 OAuth 로그인 처리 (토큰 -> 사용자 정보 -> DB 조회/가입 -> JWT 발급)
        AuthRespDTO authResp;
        try {
            authResp = googleService.googleLogin(code);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("구글 로그인 처리 실패: " + e.getMessage());
        }

        // 3) 최종적으로 JWT 반환
        return ResponseEntity.ok(authResp);
    }
}
