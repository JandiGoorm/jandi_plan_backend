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

/**
 * 사용자 관련 비즈니스 로직을 담당하는 서비스 클래스.
 * <p>
 * 이 클래스는 회원가입, 로그인, 이메일 인증, 비밀번호 재발급, 사용자 상세 정보 조회 등의 기능을 제공한다.
 * 추가적으로, 프로필 사진 조회를 위해 ProfileImageService를 주입받아 사용한다.
 * </p>
 */
@Service
@Transactional
public class UserService {

    // 사용자 정보 데이터베이스 작업을 위한 UserRepository
    private final UserRepository userRepository;
    // 이메일 발송 기능을 담당하는 EmailService (이메일 전송 로직을 포함)
    private final EmailService emailService;
    // 사용자 비밀번호 암호화를 위한 PasswordEncoder
    private final PasswordEncoder passwordEncoder;
    // JWT 토큰 생성 및 검증을 위한 JwtTokenProvider
    private final JwtTokenProvider jwtTokenProvider;
    // 프로필 사진 관련 기능(프로필 사진 조회 등)을 제공하는 ProfileImageService
    private final ProfileImageService profileImageService;

    // 이메일 인증 URL (애플리케이션 설정 파일에 정의된 값, 예: "https://example.com/verify")
    @Value("${app.verify.url}")
    private String verifyUrl;

    /**
     * 생성자 주입을 통해 필요한 의존성들을 주입받는다.
     *
     * @param userRepository     사용자 관련 DB 작업을 위한 Repository
     * @param emailService       이메일 발송을 위한 서비스
     * @param passwordEncoder    비밀번호 암호화를 위한 인코더
     * @param jwtTokenProvider   JWT 토큰 생성을 위한 프로바이더
     * @param profileImageService 프로필 사진 관련 기능을 제공하는 서비스
     */
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
     * <p>
     * 1. 입력받은 이메일이 이미 존재하는지 확인한다.<br>
     * 2. 새로운 사용자 엔티티를 생성하고, 회원가입 정보를 설정한다.<br>
     *    - 비밀번호는 암호화하여 저장<br>
     *    - 생성일, 업데이트일은 현재 시각으로 설정<br>
     *    - 인증 여부(verified)는 false, 신고 여부(reported)는 기본값 false로 설정<br>
     * 3. 이메일 인증을 위한 고유 토큰과 만료 시간을 생성하여 사용자 엔티티에 저장한다.<br>
     * 4. 사용자 정보를 데이터베이스에 저장한다.<br>
     * 5. 이메일 인증 링크를 포함한 이메일을 발송한다.
     * </p>
     *
     * @param dto 회원가입에 필요한 정보가 담긴 DTO
     * @return 저장된 사용자 엔티티
     * @throws RuntimeException 이미 존재하는 이메일일 경우 예외 발생
     */
    public User registerUser(UserRegisterDTO dto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 새로운 사용자 객체 생성 후 입력값 세팅
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        // 입력받은 비밀번호를 암호화하여 저장
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // 생성일과 업데이트일을 현재 시각으로 설정
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 이메일 인증 전 상태로 설정
        user.setVerified(false);
        // 신고 여부(reported)를 기본값 false로 설정
        user.setReported(false);

        // 이메일 인증을 위한 고유 토큰 생성 및 만료 시간 설정
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpires(LocalDateTime.now().plusHours(24));

        // 사용자 정보를 DB에 저장
        userRepository.save(user);

        // 이메일 인증 URL 생성 (예: https://example.com/verify?token=토큰값)
        String verifyLink = verifyUrl + "?token=" + token;
        String subject = "[회원가입] 이메일 인증 안내";
        String text = "안녕하세요.\n"
                + "아래 링크를 클릭하면 이메일 인증이 완료됩니다.\n\n"
                + verifyLink
                + "\n\n인증은 24시간 이내에 완료해주세요.";
        // 인증 이메일 발송
        emailService.sendSimpleMail(user.getEmail(), subject, text);

        return user;
    }

    /**
     * 로그인 기능을 수행하는 메서드.
     * <p>
     * 1. 입력된 이메일로 사용자 정보를 조회한다.<br>
     * 2. 입력된 비밀번호와 저장된 암호화된 비밀번호를 비교하여 일치 여부를 확인한다.<br>
     * 3. 이메일 인증 여부를 확인한다.<br>
     * 4. JWT 토큰을 생성하여 반환한다.
     * </p>
     *
     * @param dto 로그인에 필요한 이메일과 비밀번호가 담긴 DTO
     * @return JWT 토큰을 포함하는 인증 응답 DTO
     * @throws RuntimeException 이메일 미존재, 비밀번호 불일치, 또는 미인증인 경우 예외 발생
     */
    public AuthResponse login(UserLoginDTO dto) {
        // 이메일로 사용자 정보 조회
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
        User user = optionalUser.get();

        // 입력된 비밀번호와 암호화된 비밀번호 비교
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
     * <p>
     * 1. 입력받은 토큰으로 사용자 정보를 조회한다.<br>
     * 2. 토큰의 만료 여부를 확인한다.<br>
     * 3. 인증 완료 상태로 업데이트하고, 인증 토큰 및 만료 시간을 초기화한다.<br>
     * 4. DB에 변경사항을 저장한다.
     * </p>
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

        // 토큰 만료 여부 체크
        if (user.getTokenExpires() != null && user.getTokenExpires().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 이메일 인증 완료 처리 및 인증 관련 정보 초기화
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpires(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    /**
     * 비밀번호 찾기 기능을 수행하는 메서드.
     * <p>
     * 1. 입력된 이메일로 사용자 정보를 조회한다.<br>
     * 2. 임시 비밀번호를 생성하고 암호화하여 사용자 정보에 업데이트한다.<br>
     * 3. 임시 비밀번호 안내 이메일을 발송한다.
     * </p>
     *
     * @param email 비밀번호 재발급 받을 사용자의 이메일
     * @throws RuntimeException 해당 이메일의 사용자가 없는 경우 예외 발생
     */
    public void forgotPassword(String email) {
        // 이메일로 사용자 조회
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("해당 이메일을 사용하는 사용자가 없습니다.");
        }
        User user = optionalUser.get();
        // 임시 비밀번호 생성 (UUID의 앞 8자리 사용)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        // 생성된 임시 비밀번호를 암호화하여 저장
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 이메일 발송을 위한 제목 및 본문 구성
        String subject = "[비밀번호 찾기] 임시 비밀번호 안내";
        String text = "임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경하세요.";
        emailService.sendSimpleMail(email, subject, text);
    }

    /**
     * 인증된 사용자의 상세 정보를 조회하는 메서드.
     * <p>
     * 1. 사용자 ID를 기반으로 사용자 정보를 조회한다.<br>
     * 2. 조회된 사용자 정보를 DTO에 담는다.<br>
     * 3. ProfileImageService를 이용하여 프로필 사진 정보를 조회하고, 공개 URL을 생성하여 DTO에 포함시킨다.
     * </p>
     *
     * @param userId 사용자 고유 ID
     * @return UserInfoResponseDto 사용자 상세 정보 DTO
     * @throws RuntimeException 사용자를 찾을 수 없는 경우 예외 발생
     */
    public UserInfoResponseDto getUserInfo(Integer userId) {
        // 사용자 정보를 DB에서 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        // DTO 생성 및 사용자 정보 설정
        UserInfoResponseDto dto = new UserInfoResponseDto();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setUsername(user.getUserName());
        dto.setVerified(user.getVerified());
        dto.setReported(user.getReported());

        // 프로필 사진 정보 조회: 프로필 사진은 대상 타입 "profile"이고, targetId는 사용자 ID
        String profileImageUrl = profileImageService.getProfileImage(userId)
                .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                .orElse(null);
        dto.setProfileImageUrl(profileImageUrl);
        return dto;
    }
}
