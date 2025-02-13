package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.dto.*;
import com.jandi.plan_backend.user.service.UserService;
import com.jandi.plan_backend.user.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    /** 회원가입 -> 인증 링크 이메일 전송 */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("회원가입 완료! 이메일의 링크를 클릭하면 인증이 완료됩니다.");
    }

    /** 로그인 - JWT 토큰 발급 */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("Login attempt for email: {}", userLoginDTO.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword())
        );
        String token = jwtTokenProvider.createToken(userLoginDTO.getEmail());
        log.info("User {} logged in successfully, JWT token generated", userLoginDTO.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    /** 이메일 인증 */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyByToken(@RequestParam("token") String token) {
        boolean result = userService.verifyEmailByToken(token);
        if (result) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다!");
        }
        return ResponseEntity.badRequest().body("인증에 실패했습니다. 토큰이 유효하지 않거나 만료되었습니다.");
    }

    /** 비밀번호 찾기 (임시 비밀번호 발급) */
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.forgotPassword(email);
        return ResponseEntity.ok("임시 비밀번호가 발급되었습니다. 이메일을 확인하세요.");
    }
}
