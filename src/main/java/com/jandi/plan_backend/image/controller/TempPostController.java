package com.jandi.plan_backend.image.controller;

import com.jandi.plan_backend.commu.community.dto.TempPostRespDTO;
import com.jandi.plan_backend.image.TempPostIdGenerator;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.image.service.InMemoryTempPostService;
import com.jandi.plan_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/temp")
@RequiredArgsConstructor
public class TempPostController {

    private final InMemoryTempPostService inMemoryTempPostService;
    private final ValidationUtil validationUtil;

    /**
     * 임시 postId 발급 API
     */
    @PostMapping
    public ResponseEntity<TempPostRespDTO> createTempPost(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();

        // 사용자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 음수 int 생성
        int tempPostId = TempPostIdGenerator.generateNegativeId();

        // 서버 메모리에 tempPostId → userId 등록
        inMemoryTempPostService.putTempId(tempPostId, user.getUserId());

        // 응답
        TempPostRespDTO resp = new TempPostRespDTO(tempPostId, "임시 postId(음수 int) 발급 성공");
        return ResponseEntity.ok(resp);
    }
}
