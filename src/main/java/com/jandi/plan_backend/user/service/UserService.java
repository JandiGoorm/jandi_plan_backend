package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.dto.AuthResponse;
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

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    // ProfileImageService는 프로필 사진 조회 기능을 제공 (이미지 업로드 관련 코드는 별도 ProfileImageService에서 관리)
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
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpires(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String verifyLink = verifyUrl + "?token=" + token;
        String subject = "[회원가입] 이메일 인증 안내";
        String text = "안녕하세요.\n" +
                "아래 링크를 클릭하면 이메일 인증이 완료됩니다.\n\n" +
                verifyLink + "\n\n인증은 24시간 이내에 완료해주세요.";
        emailService.sendSimpleMail(user.getEmail(), subject, text);

        return user;
    }

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
        String token = jwtTokenProvider.createToken(dto.getEmail());
        return new AuthResponse(token);
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

    /**
     * 인증된 사용자의 상세 정보를 조회하는 메서드.
     * 추가로, 프로필 사진 정보(대상 타입 "profile", targetId가 사용자 ID인 이미지)를 조회하여 공개 URL을 생성합니다.
     *
     * @param userId 사용자 고유 ID
     * @return UserInfoResponseDto 사용자 상세 정보 DTO
     */
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
        // 프로필 사진 조회: ProfileImageService에서 대상 타입 "profile"과 targetId를 이용해 조회
        String profileImageUrl = profileImageService.getProfileImage(userId)
                .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                .orElse(null);
        dto.setProfileImageUrl(profileImageUrl);
        return dto;
    }
}
