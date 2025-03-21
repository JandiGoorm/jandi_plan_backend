package com.jandi.plan_backend.socialLogin.controller;

import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.socialLogin.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;

    /**
     * 프론트에서 code를 쿼리 파라미터로 GET 요청
     */
    @GetMapping("/kakaoLogin")
    public ResponseEntity<?> kakaoLogin(@RequestParam(required = false) String code) {
        // 1) code가 없으면 에러
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("No code provided");
        }

        // 2) 카카오 OAuth 로그인 처리 (카카오에 토큰 요청 -> 사용자 정보 -> DB 가입/조회 -> JWT 발급)
        AuthRespDTO authResp;
        try {
            authResp = kakaoService.kakaoLogin(code);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("카카오 로그인 처리 실패: " + e.getMessage());
        }

        // 3) 최종적으로 우리 서비스 JWT(액세스 토큰, 리프레시 토큰 등) 반환
        return ResponseEntity.ok(authResp);
    }
}
