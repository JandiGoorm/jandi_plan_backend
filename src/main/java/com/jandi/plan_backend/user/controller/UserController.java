package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.security.CustomUserDetails;
import com.jandi.plan_backend.user.dto.*;
import com.jandi.plan_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserController는 회원가입, 로그인, 이메일 인증, 비밀번호 찾기,
 * 사용자 정보 조회, 비밀번호 변경, 그리고 리프레시 토큰 갱신 기능을 제공하는 REST 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("회원가입 완료! 이메일의 링크를 클릭하면 인증이 완료됨");
    }

    @GetMapping("/register/checkEmail")
    public ResponseEntity<?> checkEmail(
            @RequestParam ("email") String email
    ) {
        if(email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("이메일을 입력해주세요!");
        }

        boolean isPossibleEmail = !userService.isExistEmail(email);
        String respMsg = email + "은/는 " + ((isPossibleEmail) ?
                "사용 가능한 이메일입니다" : "이미 사용중인 이메일입니다");

        return (isPossibleEmail) ? ResponseEntity.ok(respMsg) : ResponseEntity.badRequest().body(respMsg);
    }

    @GetMapping("/register/checkName")
    public ResponseEntity<?> checkName(
            @RequestParam ("name") String name
    ) {
        if(name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body("닉네임을 입력해주세요!");
        }

        boolean isPossibleEmail = !userService.isExistUserName(name);
        String respMsg = name + "은/는 " + ((isPossibleEmail) ?
                "사용 가능한 닉네임입니다" : "이미 사용중인 닉네임입니다");

        return (isPossibleEmail) ? ResponseEntity.ok(respMsg) : ResponseEntity.badRequest().body(respMsg);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRespDTO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        log.info("로그인 시도, 이메일: {}", userLoginDTO.getEmail());
        AuthRespDTO authRespDTO = userService.login(userLoginDTO);
        log.info("로그인 성공, 이메일: {}, JWT 토큰 생성됨", userLoginDTO.getEmail());
        return ResponseEntity.ok(authRespDTO);
    }

    /**
     * 리프레시 토큰 갱신 엔드포인트.
     * 클라이언트가 보낸 리프레시 토큰을 검증하고, 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     *
     * @param request JSON 형식의 요청 본문 (키: "refreshToken")
     * @return 새로 발급된 토큰들을 포함한 AuthRespDTO 객체
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            AuthRespDTO authRespDTO = userService.refreshToken(refreshToken);
            return ResponseEntity.ok(authRespDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "리프레시 토큰이 유효하지 않거나 만료되었습니다."));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyByToken(@RequestParam("token") String token) {
        boolean result = userService.verifyEmailByToken(token);
        if (result) {
            return ResponseEntity.ok("이메일 인증 완료됨");
        }
        return ResponseEntity.badRequest().body("인증 실패, 토큰이 유효하지 않거나 만료됨");
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.forgotPassword(email);
        return ResponseEntity.ok("임시 비밀번호 발급됨, 이메일 확인");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal com.jandi.plan_backend.security.CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Integer userId = customUserDetails.getUserId();
        UserInfoRespDTO dto = userService.getUserInfo(userId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal com.jandi.plan_backend.security.CustomUserDetails customUserDetails,
            @Valid @RequestBody ChangePasswordDTO dto) {
        if (customUserDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        String email = customUserDetails.getUsername();
        try {
            userService.changePassword(email, dto);
            return ResponseEntity.ok(Map.of("message", "비밀번호 변경이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 회원 탈퇴(계정 삭제) 엔드포인트.
     * 인증된 사용자가 자신의 계정을 삭제할 수 있도록 한다.
     *
     * URL: DELETE /api/users/del-user
     * 헤더: Authorization: Bearer {accessToken}
     *
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 탈퇴 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/del-user")
    public ResponseEntity<?> delUser(@AuthenticationPrincipal com.jandi.plan_backend.security.CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }
        String email = customUserDetails.getUsername();
        try {
            userService.deleteUser(email);
            return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 닉네임 변경 API
     * PATCH /api/users/change-username
     * Body: { "newUserName": "원하는 닉네임" }
     */
    @PatchMapping("/change-username")
    public ResponseEntity<?> changeUserName(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UserNameUpdateDTO dto
    ) {
        // 1) 인증 확인
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        // 2) 서비스 로직 호출
        String email = customUserDetails.getUsername();
        try {
            userService.changeUserName(email, dto.getNewUserName());
            return ResponseEntity.ok(Map.of("message", "닉네임 변경이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
