package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * 회원가입
     * -> verificationToken, tokenExpires 추가
     * -> 인증 링크를 이메일로 전송
     */
    public User registerUser(UserRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // 실제로는 암호화 권장

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setVerified(false);

        // 인증 토큰 생성
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        // 예: 24시간 뒤 만료
        user.setTokenExpires(LocalDateTime.now().plusHours(24));

        // DB 저장
        userRepository.save(user);

        // 이메일 발송
        String subject = "[회원가입] 이메일 인증 안내";
        // 인증 링크 예시: http://localhost:8080/api/users/verify?token=...
        String verifyLink = "http://localhost:8080/api/users/verify?token=" + token;
        String text = "안녕하세요.\n"
                + "아래 링크를 클릭하면 이메일 인증이 완료됩니다.\n\n"
                + verifyLink
                + "\n\n인증은 24시간 이내에 완료해주세요.";

        emailService.sendSimpleMail(user.getEmail(), subject, text);

        return user;
    }

    /**
     * 로그인 로직 (단순 예시)
     */
    public User login(UserLoginDTO dto) {
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
        User user = optionalUser.get();

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        // 인증 안 된 계정은 제한할 수도 있음
        // if (!user.getVerified()) {
        //     throw new RuntimeException("이메일 인증이 필요합니다.");
        // }

        return user;
    }

    /**
     * 인증 링크 클릭 시 처리
     */
    public boolean verifyEmailByToken(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isEmpty()) {
            return false;
        }
        User user = optionalUser.get();

        // 토큰 만료 검사 (선택)
        if (user.getTokenExpires() != null &&
                user.getTokenExpires().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 인증 처리
        user.setVerified(true);
        user.setVerificationToken(null); // 재사용 방지
        user.setTokenExpires(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    /**
     * 비밀번호 찾기 / 임시 비밀번호 발급
     */
    public void forgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("해당 이메일을 사용하는 사용자가 없습니다.");
        }
        User user = optionalUser.get();

        // 임시 비밀번호 생성
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(tempPassword); // 실제로는 암호화 권장
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 이메일 전송
        String subject = "[비밀번호 찾기] 임시 비밀번호 안내";
        String text = "임시 비밀번호: " + tempPassword
                + "\n로그인 후 반드시 비밀번호를 변경하세요.";
        emailService.sendSimpleMail(email, subject, text);
    }
}
