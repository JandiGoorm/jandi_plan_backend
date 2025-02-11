package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service  // 해당 클래스를 서비스 컴포넌트로 등록. 비즈니스 로직 처리를 담당함.
@Transactional  // 메서드 실행 중 예외 발생 시 트랜잭션을 롤백 처리함.
public class UserService {

    // UserRepository: 사용자 데이터베이스 작업 인터페이스. 사용자 엔티티에 대한 CRUD 작업을 처리.
    private final UserRepository userRepository;

    // EmailService: 이메일 전송 기능을 담당하는 서비스.
    private final EmailService emailService;

    // PasswordEncoder: 사용자의 비밀번호를 암호화하고 검증하는 데 사용.
    private final PasswordEncoder passwordEncoder;

    // application.properties 파일에 설정한 'app.verify.url' 값을 주입받음.
    @Value("${app.verify.url}")
    private String verifyUrl;

    // 생성자를 통해 필요한 의존성들을 주입받음.
    public UserService(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입 처리 메서드
     *
     * 1. 입력받은 이메일로 이미 가입된 사용자가 있는지 확인.
     *    - 이미 존재하면 예외를 발생시켜 회원가입 진행 중단.
     *
     * 2. 새로운 User 객체 생성 후 DTO로부터 입력값 설정.
     *    - 비밀번호는 raw 값 그대로 저장하지 않고, BCryptPasswordEncoder를 통해 암호화한 후 저장.
     *    - 가입 시간(createdAt)과 수정 시간(updatedAt)을 현재 시간으로 설정.
     *    - 기본 인증 상태는 false로 설정 (이메일 인증 전임).
     *
     * 3. 이메일 인증을 위한 토큰을 생성하고, 토큰 만료 시간을 현재 시간 기준 24시간 후로 설정.
     *
     * 4. 생성한 사용자 정보를 데이터베이스에 저장.
     *
     * 5. application.properties에 설정된 verifyUrl과 생성된 토큰을 조합하여 인증 링크를 생성.
     *
     * 6. 사용자 이메일로 인증 메일 발송.
     *
     * @param dto 회원가입 요청에 필요한 사용자 정보 (이메일, 비밀번호, 이름 등)를 담은 DTO 객체.
     * @return 생성된 사용자 객체를 반환.
     */
    public User registerUser(UserRegisterDTO dto) {
        // 입력한 이메일이 이미 존재하는지 확인. 존재하면 중복 가입 방지.
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 새로운 사용자 객체 생성 및 정보 설정
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        // 입력받은 비밀번호를 암호화하여 저장. 평문 저장을 피하기 위함.
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // 현재 시간을 가입 및 수정 시간으로 설정
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 초기 가입 시 이메일 인증 상태는 false로 설정
        user.setVerified(false);

        // 이메일 인증을 위한 랜덤 토큰 생성
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        // 토큰 만료 시간은 현재 시간으로부터 24시간 후로 설정
        user.setTokenExpires(LocalDateTime.now().plusHours(24));

        // 사용자 정보를 데이터베이스에 저장
        userRepository.save(user);

        // application.properties에 등록된 verifyUrl을 이용해 인증 링크를 생성
        String verifyLink = verifyUrl + "?token=" + token;

        // 이메일 전송 내용 구성
        String subject = "[회원가입] 이메일 인증 안내";
        String text = "안녕하세요.\n"
                + "아래 링크를 클릭하면 이메일 인증이 완료됩니다.\n\n"
                + verifyLink
                + "\n\n인증은 24시간 이내에 완료해주세요.";

        // EmailService를 사용해 사용자의 이메일로 인증 메일 전송
        emailService.sendSimpleMail(user.getEmail(), subject, text);

        // 생성된 사용자 객체 반환
        return user;
    }

    /**
     * 로그인 처리 메서드
     *
     * 1. 입력받은 이메일을 통해 사용자가 존재하는지 조회.
     *    - 사용자가 존재하지 않으면 예외를 발생시킴.
     *
     * 2. 입력받은 비밀번호와 데이터베이스에 저장된 암호화된 비밀번호를 비교.
     *    - 비밀번호가 일치하지 않으면 예외를 발생시킴.
     *
     * 3. 사용자가 이메일 인증을 완료했는지 확인.
     *    - 인증되지 않은 경우 예외를 발생시킴.
     *
     * 4. 모든 검증 통과 시 사용자 객체 반환.
     *
     * @param dto 로그인 요청 시 입력한 이메일과 비밀번호를 담은 DTO 객체.
     * @return 로그인 성공한 사용자 객체를 반환.
     */
    public User login(UserLoginDTO dto) {
        // 입력받은 이메일을 기준으로 사용자 정보 조회
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
        User user = optionalUser.get();

        // 입력받은 비밀번호와 데이터베이스에 저장된 암호화된 비밀번호 비교
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        // 이메일 인증이 완료되었는지 확인
        if (!user.getVerified()) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }

        // 모든 검증 통과 후 사용자 객체 반환
        return user;
    }

    /**
     * 이메일 인증 처리 메서드
     *
     * 1. 전달받은 토큰 값을 기반으로 사용자 정보를 조회.
     *    - 토큰에 해당하는 사용자가 없으면 false 반환.
     *
     * 2. 조회된 사용자의 토큰 만료 시간을 확인.
     *    - 현재 시간 이후로 만료 시간이 설정되어 있지 않거나 이미 만료된 경우 false 반환.
     *
     * 3. 인증 성공 시 사용자의 인증 상태를 true로 변경하고, 인증 토큰과 만료 시간을 초기화.
     *    - 재사용 방지를 위해 토큰 값과 만료 시간을 null로 설정.
     *
     * 4. 변경된 사용자 정보를 데이터베이스에 저장 후 true 반환.
     *
     * @param token 이메일 인증에 사용된 토큰 값.
     * @return 인증 성공 시 true, 실패 시 false 반환.
     */
    public boolean verifyEmailByToken(String token) {
        // 토큰 값으로 사용자 조회
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isEmpty()) {
            return false;
        }
        User user = optionalUser.get();

        // 토큰 만료 여부 검사: 만료 시간이 현재 시간보다 이전이면 토큰 만료
        if (user.getTokenExpires() != null && user.getTokenExpires().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 사용자 인증 상태 업데이트: 인증 완료 처리
        user.setVerified(true);
        // 인증 토큰과 만료 시간을 null로 설정하여 재사용 방지
        user.setVerificationToken(null);
        user.setTokenExpires(null);
        // 수정 시간을 현재 시간으로 업데이트
        user.setUpdatedAt(LocalDateTime.now());
        // 변경 사항을 데이터베이스에 저장
        userRepository.save(user);

        // 인증 성공 반환
        return true;
    }

    /**
     * 비밀번호 찾기 및 임시 비밀번호 발급 메서드
     *
     * 1. 입력받은 이메일로 사용자 조회.
     *    - 해당 이메일을 사용하는 사용자가 없으면 예외 발생.
     *
     * 2. UUID를 이용해 8자리 임시 비밀번호 생성.
     *
     * 3. 생성된 임시 비밀번호를 암호화하여 사용자 정보에 저장.
     *
     * 4. 사용자 정보 수정 시간을 현재 시간으로 업데이트하고, 데이터베이스에 저장.
     *
     * 5. 임시 비밀번호를 포함한 이메일 내용을 구성하여 전송.
     *
     * @param email 임시 비밀번호를 요청한 사용자의 이메일 주소.
     */
    public void forgotPassword(String email) {
        // 입력받은 이메일을 기준으로 사용자 정보 조회
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("해당 이메일을 사용하는 사용자가 없습니다.");
        }
        User user = optionalUser.get();

        // UUID를 사용해 임시 비밀번호 생성 후 앞의 8자리만 사용
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        // 생성한 임시 비밀번호를 암호화하여 사용자 비밀번호로 설정
        user.setPassword(passwordEncoder.encode(tempPassword));
        // 비밀번호 변경 시간을 현재 시간으로 업데이트
        user.setUpdatedAt(LocalDateTime.now());
        // 변경된 사용자 정보를 데이터베이스에 저장
        userRepository.save(user);

        // 임시 비밀번호 안내 이메일 제목 및 내용 구성
        String subject = "[비밀번호 찾기] 임시 비밀번호 안내";
        String text = "임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경하세요.";

        // EmailService를 통해 사용자의 이메일로 임시 비밀번호 전송
        emailService.sendSimpleMail(email, subject, text);
    }
}
