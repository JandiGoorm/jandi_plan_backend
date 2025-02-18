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

    /**
     * 회원가입을 수행하는 메서드.
     * 1. 입력받은 이메일이 이미 존재하는지 확인한다.
     * 2. 새 사용자 엔티티를 생성, 비밀번호 암호화, 생성/업데이트 일시 설정,
     *    인증 여부(verified)는 false, 신고 여부(reported)는 기본값 false로 설정한다.
     * 3. 이메일 인증을 위한 토큰과 만료 시간을 생성하여 저장하고,
     *    DB에 저장 후 인증 이메일을 발송한다.
     *
     * @param dto 회원가입 정보를 담은 DTO
     * @return 저장된 사용자 엔티티
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
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setVerified(false);
        // 신고 여부(reported)를 기본값 false로 설정
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
     * 로그인 기능을 수행하는 메서드.
     * 1. 입력된 이메일로 사용자 조회 후 비밀번호 및 인증 여부 확인,
     * 2. JWT 토큰 생성하여 반환.
     *
     * @param dto 로그인 정보를 담은 DTO
     * @return JWT 토큰을 포함한 AuthResponse 객체
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
        String token = jwtTokenProvider.createToken(dto.getEmail());
        return new AuthResponse(token);
    }

    /**
     * 이메일 인증 토큰을 이용해 사용자의 이메일 인증을 수행하는 메서드.
     *
     * @param token 이메일 인증 토큰
     * @return 인증 성공 시 true, 실패 시 false
     */
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

    /**
     * 비밀번호 찾기 기능을 수행하는 메서드.
     *
     * @param email 비밀번호 재발급 받을 사용자의 이메일
     */
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
     * 1. 사용자 ID로 DB에서 사용자 정보를 조회하고,
     * 2. ProfileImageService를 통해 프로필 사진 정보를 조회하여 공개 URL을 생성 후 DTO에 포함시킵니다.
     *
     * @param userId 사용자 고유 ID
     * @return 사용자 상세 정보를 담은 UserInfoResponseDto
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

        // 프로필 사진 조회 (대상 타입 "profile", targetId = userId)
        String profileImageUrl = profileImageService.getProfileImage(userId)
                .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                .orElse(null);
        dto.setProfileImageUrl(profileImageUrl);
        return dto;
    }

    /**
     * 비밀번호 변경 기능을 수행하는 메서드.
     * 1. 인증된 사용자의 이메일로 사용자 정보를 조회합니다.
     * 2. 입력받은 현재 비밀번호와 저장된 암호화된 비밀번호를 비교합니다.
     * 3. 현재 비밀번호가 일치하면 새 비밀번호로 업데이트합니다.
     *
     * @param email 인증된 사용자의 이메일
     * @param dto ChangePasswordDTO (현재 비밀번호, 새 비밀번호)
     * @throws RuntimeException 현재 비밀번호가 올바르지 않거나, 사용자를 찾을 수 없는 경우 예외 발생
     */
    public void changePassword(String email, com.jandi.plan_backend.user.dto.ChangePasswordDTO dto) {
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
}
