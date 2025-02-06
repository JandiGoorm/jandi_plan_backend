package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
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
     * -> 인증번호(emailVerificationCode)를 생성 후 DB에 저장
     * -> 이메일로 인증번호 전송
     */
    public User registerUser(UserRegisterDTO dto) {
        // 1) 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        // 2) User 엔티티 생성
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        // 실제로는 BCrypt 등으로 암호화 권장
        user.setPassword(dto.getPassword());

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setVerified(false);

        // 3) 인증번호 생성
        String verificationCode = generateVerificationCode();
        user.setEmailVerificationCode(verificationCode);

        // 4) DB 저장
        userRepository.save(user);

        // 5) 이메일 발송
        String subject = "[회원가입] 이메일 인증번호 안내";
        String text = "인증번호는 [" + verificationCode + "] 입니다.\n"
                + "해당 번호를 입력하여 이메일 인증을 완료하세요.";
        emailService.sendSimpleMail(user.getEmail(), subject, text);

        return user;
    }

    /**
     * 로그인 (단순 예시)
     */
    public User login(UserLoginDTO dto) {
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
        User user = optionalUser.get();

        // 비밀번호 검증
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        // 이메일 인증 여부 체크
        if (!Boolean.TRUE.equals(user.getVerified())) {
            // throw new RuntimeException("이메일 인증이 필요합니다.");
            // 여기서는 에러 던지지 않고, 미인증 사용자도 로그인 시킬지 여부는 정책에 따라 결정
        }

        // 로그인 성공 -> JWT 발급 등
        return user;
    }

    /**
     * 인증번호 검증
     */
    public boolean verifyEmailCode(String code) {
        // code가 일치하는 유저 조회
        Optional<User> optionalUser = userRepository.findByEmailVerificationCode(code);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setVerified(true);
            // 인증 성공 후 재사용 방지
            user.setEmailVerificationCode(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * 비밀번호 찾기(임시 비밀번호 발급)
     */
    public void forgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("해당 이메일을 사용하는 사용자가 없습니다.");
        }
        User user = optionalUser.get();

        // 1) 임시 비밀번호 생성
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(tempPassword); // 실제로는 암호화 권장
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 2) 이메일 발송
        String subject = "[비밀번호 찾기] 임시 비밀번호 안내";
        String text = "임시 비밀번호는 [" + tempPassword + "] 입니다.\n"
                + "로그인 후 비밀번호를 변경해주세요.";
        emailService.sendSimpleMail(email, subject, text);
    }

    /**
     * 6자리 숫자 인증번호 생성 (예시)
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000~999999
        return String.valueOf(code);
    }
}
