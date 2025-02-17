package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.dto.AuthResponse;
import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 관련 비즈니스 로직을 담당하는 서비스 클래스.
 *
 * 회원가입, 로그인, 이메일 인증, 비밀번호 재발급 등 사용자 관련 기능을 구현.
 */
@Service
@Transactional
public class UserService {

    // 사용자 정보 DB 처리를 위한 UserRepository 주입
    private final UserRepository userRepository;
    // 이메일 발송 기능을 수행하는 EmailService 주입
    private final EmailService emailService;
    // 비밀번호 암호화를 위한 PasswordEncoder 주입
    private final PasswordEncoder passwordEncoder;
    // JWT 토큰 생성을 위한 JwtTokenProvider 주입
    private final JwtTokenProvider jwtTokenProvider;

    // 이메일 인증 URL (예: "https://example.com/verify")
    @Value("${app.verify.url}")
    private String verifyUrl;

    /**
     * 생성자 주입.
     *
     * @param userRepository     사용자 관련 DB 작업을 위한 레포지토리
     * @param emailService       이메일 발송을 위한 서비스
     * @param passwordEncoder    비밀번호 암호화를 위한 인코더
     * @param jwtTokenProvider   JWT 토큰 생성을 위한 프로바이더
     */
    public UserService(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 회원가입을 수행하는 메서드.
     *
     * 1. 입력받은 이메일이 이미 존재하는지 확인한다.
     * 2. 새로운 사용자 엔티티를 생성하고, 입력값을 세팅한다.
     * 3. 비밀번호는 암호화하여 저장한다.
     * 4. 회원가입 시 이메일 인증을 위한 토큰을 생성하여 사용자 엔티티에 저장한다.
     * 5. DB에 사용자를 저장한다.
     * 6. 인증 링크를 포함한 이메일을 발송한다.
     *
     * @param dto 회원가입에 필요한 정보가 담긴 DTO
     * @return 저장된 사용자 엔티티
     * @throws RuntimeException 이미 존재하는 이메일인 경우 예외 발생
     */
    public User registerUser(UserRegisterDTO dto) {
        // 이미 존재하는 이메일인지 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 새로운 사용자 객체 생성 후 입력값 세팅
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        // 비밀번호는 암호화 후 저장
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 이메일 인증 전 상태로 설정
        user.setVerified(false);

        // 이메일 인증을 위한 고유 토큰 생성
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        // 토큰 만료 시간은 현재 시각에서 24시간 후로 설정
        user.setTokenExpires(LocalDateTime.now().plusHours(24));

        // 사용자 정보를 DB에 저장
        userRepository.save(user);

        // 인증 URL 생성 (예: https://example.com/verify?token=토큰값)
        String verifyLink = verifyUrl + "?token=" + token;
        String subject = "[회원가입] 이메일 인증 안내";
        // 이메일 본문 내용 구성
        String text = "안녕하세요.\n"
                + "아래 링크를 클릭하면 이메일 인증이 완료됩니다.\n\n"
                + verifyLink
                + "\n\n인증은 24시간 이내에 완료해주세요.";
        // 이메일 발송
        emailService.sendSimpleMail(user.getEmail(), subject, text);

        return user;
    }

    /**
     * 사용자의 로그인 기능을 수행하는 메서드.
     *
     * 1. 입력된 이메일로 사용자 정보를 조회한다.
     * 2. 비밀번호가 일치하는지 확인한다.
     * 3. 이메일 인증 여부를 확인한다.
     * 4. JWT 토큰을 생성하여 반환한다.
     *
     * @param dto 로그인에 필요한 이메일과 비밀번호가 담긴 DTO
     * @return JWT 토큰을 포함하는 인증 응답 DTO
     * @throws RuntimeException 이메일이 존재하지 않거나, 비밀번호 불일치, 또는 이메일 미인증인 경우 예외 발생
     */
    public AuthResponse login(UserLoginDTO dto) {
        // 이메일로 사용자 정보 조회
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
        User user = optionalUser.get();

        // 비밀번호 비교 (암호화된 비밀번호와 입력값 비교)
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        // 이메일 인증 여부 확인
        if (!user.getVerified()) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(dto.getEmail());
        return new AuthResponse(token);
    }

    /**
     * 이메일 인증 토큰을 이용해 사용자의 이메일 인증을 수행하는 메서드.
     *
     * 1. 토큰으로 사용자 정보를 조회한다.
     * 2. 토큰 만료 여부를 확인한다.
     * 3. 인증 완료 상태로 업데이트 후 DB에 저장한다.
     *
     * @param token 이메일 인증 토큰
     * @return 인증 성공 시 true, 실패 시 false
     */
    public boolean verifyEmailByToken(String token) {
        // 토큰으로 사용자 조회
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isEmpty()) {
            return false;
        }
        User user = optionalUser.get();

        // 토큰 만료 여부 체크 (토큰 만료 시간이 현재 시간보다 이전이면 만료된 것으로 간주)
        if (user.getTokenExpires() != null && user.getTokenExpires().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 이메일 인증 완료 처리
        user.setVerified(true);
        // 인증 토큰과 만료 시간은 삭제
        user.setVerificationToken(null);
        user.setTokenExpires(null);
        // 업데이트 시각 갱신
        user.setUpdatedAt(LocalDateTime.now());

        // 변경 사항을 DB에 저장
        userRepository.save(user);
        return true;
    }

    /**
     * 비밀번호 찾기 기능을 수행하는 메서드.
     *
     * 1. 입력된 이메일로 사용자 정보를 조회한다.
     * 2. 임시 비밀번호를 생성한다.
     * 3. 임시 비밀번호를 암호화하여 사용자 정보에 업데이트한다.
     * 4. 임시 비밀번호를 안내하는 이메일을 발송한다.
     *
     * @param email 비밀번호를 재발급 받을 사용자의 이메일
     * @throws RuntimeException 해당 이메일의 사용자가 없을 경우 예외 발생
     */
    public void forgotPassword(String email) {
        // 이메일로 사용자 조회
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("해당 이메일을 사용하는 사용자가 없습니다.");
        }
        User user = optionalUser.get();

        // 임시 비밀번호 생성 (UUID의 앞 8자리)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        // 임시 비밀번호를 암호화하여 저장
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String subject = "[비밀번호 찾기] 임시 비밀번호 안내";
        // 이메일 본문에 임시 비밀번호 포함 및 변경 안내 문구 추가
        String text = "임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경하세요.";
        emailService.sendSimpleMail(email, subject, text);
    }
}
