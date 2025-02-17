package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.dto.*;
import com.jandi.plan_backend.user.service.UserService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("회원가입 완료! 이메일의 링크를 클릭하면 인증이 완료됨");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("로그인 시도, 이메일: {}", userLoginDTO.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword())
        );
        String token = jwtTokenProvider.createToken(userLoginDTO.getEmail());
        log.info("로그인 성공, 이메일: {}, JWT 토큰 생성됨", userLoginDTO.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
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

    /**
     * 인증된 사용자의 상세 정보를 조회하는 엔드포인트.
     * JWT 토큰을 통해 인증된 사용자 정보를 기반으로 사용자 프로필 정보를 반환합니다.
     *
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 사용자 상세 정보를 담은 UserInfoResponseDto (JSON 형식)
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal com.jandi.plan_backend.security.CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Integer userId = customUserDetails.getUserId();
        UserInfoResponseDto dto = userService.getUserInfo(userId);
        return ResponseEntity.ok(dto);
    }
}
