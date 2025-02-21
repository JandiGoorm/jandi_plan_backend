package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.dto.AuthResponse;
import com.jandi.plan_backend.user.dto.ChangePasswordDTO;
import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.dto.UserInfoResponseDto;
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
 * 회원가입, 로그인, 이메일 인증, 비밀번호 재발급, 사용자 상세 정보 조회,
 * 비밀번호 변경, 리프레시 토큰 갱신 등의 기능을 제공한다.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileImageService profileImageService;

    @Value("${app.verify.url}")
    private String verifyUrl;

    public UserService(UserRepository userRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       ProfileImageService profileImageService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.profileImageService = profileImageService;
    }

    public User registerUser(UserRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setVerified(false);
        user.setReported(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpires(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        String verifyLink = verifyUrl + "?token=" + token;
        String subject = "[회원가입] 이메일 인증 안내";
        String text = "안녕하세요.\n"
                + "아래 링크를 클릭하면 이메일 인증이 완료됩니다.\n\n"
                + verifyLink
                + "\n\n인증은 24시간 이내에 완료해주세요.";
        emailService.sendSimpleMail(user.getEmail(), subject, text);
        return user;
    }

    /**
     * 로그인 기능을 수행한다.
     * 이메일과 비밀번호를 검증하고, 인증에 성공하면 액세스 토큰과 리프레시 토큰을 발급한다.
     *
     * @param dto 로그인 정보를 담은 DTO
     * @return AuthResponse 객체 (액세스 토큰, 리프레시 토큰)
     */
    public AuthResponse login(UserLoginDTO dto) {
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        if (!user.getVerified()) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }
        String accessToken = jwtTokenProvider.createToken(dto.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(dto.getEmail());
        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * 리프레시 토큰을 이용해 새 액세스 토큰(및 리프레시 토큰)을 발급한다.
     *
     * @param refreshToken 클라이언트가 전송한 리프레시 토큰
     * @return 새로 발급된 토큰들을 포함한 AuthResponse 객체
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 유효하지 않습니다.");
        }
        String email = jwtTokenProvider.getEmail(refreshToken);
        String newAccessToken = jwtTokenProvider.createToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);
        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public boolean verifyEmailByToken(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isEmpty()) {
            return false;
        }
        User user = optionalUser.get();
        if (user.getTokenExpires() != null && user.getTokenExpires().isBefore(LocalDateTime.now())) {
            return false;
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpires(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    public void forgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("해당 이메일을 사용하는 사용자가 없습니다.");
        }
        User user = optionalUser.get();
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        String subject = "[비밀번호 찾기] 임시 비밀번호 안내";
        String text = "임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경하세요.";
        emailService.sendSimpleMail(email, subject, text);
    }

    public UserInfoResponseDto getUserInfo(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        UserInfoResponseDto dto = new UserInfoResponseDto();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setUsername(user.getUserName());
        dto.setVerified(user.getVerified());
        dto.setReported(user.getReported());
        String profileImageUrl = profileImageService.getProfileImage(userId)
                .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                .orElse(null);
        dto.setProfileImageUrl(profileImageUrl);
        return dto;
    }

    public void changePassword(String email, ChangePasswordDTO dto) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public boolean isExistEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isExistUserName(String userName) {
        return userRepository.findByUserName(userName).isPresent();
    }
}
