package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 회원가입 -> 인증번호 이메일 전송 */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("회원가입 완료! 이메일을 확인하고 인증번호를 입력하세요.");
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO dto) {
        User user = userService.login(dto);
        return ResponseEntity.ok(user);
    }

    /** 인증번호 검증 -> 이메일 인증 */
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("code") String code) {
        boolean result = userService.verifyEmailCode(code);
        if (result) {
            return ResponseEntity.ok("이메일 인증 성공!");
        }
        return ResponseEntity.badRequest().body("인증번호가 유효하지 않습니다.");
    }

    /** 비밀번호 찾기 (임시 비밀번호 발급) */
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok("임시 비밀번호가 발급되었습니다. 이메일을 확인하세요.");
    }
}
