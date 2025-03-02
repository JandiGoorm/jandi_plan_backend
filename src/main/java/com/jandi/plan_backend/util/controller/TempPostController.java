package com.jandi.plan_backend.util.controller;

import com.jandi.plan_backend.commu.dto.TempPostRespDTO;
import com.jandi.plan_backend.util.TempPostIdGenerator;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.InMemoryTempPostService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/temp")
@RequiredArgsConstructor
public class TempPostController {

    private final InMemoryTempPostService inMemoryTempPostService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ValidationUtil validationUtil;

    /**
     * 임시 postId 발급 API
     */
    @PostMapping
    public ResponseEntity<TempPostRespDTO> createTempPost(@RequestHeader("Authorization") String token) {
        // 1) 토큰에서 사용자 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 2) 사용자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 3) 음수 int 생성
        int tempPostId = TempPostIdGenerator.generateNegativeId();

        // 4) 서버 메모리에 tempPostId → userId 등록
        inMemoryTempPostService.putTempId(tempPostId, user.getUserId());

        // 5) 응답
        TempPostRespDTO resp = new TempPostRespDTO(tempPostId, "임시 postId(음수 int) 발급 성공");
        return ResponseEntity.ok(resp);
    }
}
