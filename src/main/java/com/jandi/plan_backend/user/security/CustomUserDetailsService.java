package com.jandi.plan_backend.user.security;

import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService는 Spring Security에서 인증 및 권한 정보를 로드할 때 사용하는 서비스 클래스다.
 * 이 클래스는 UserDetailsService 인터페이스를 구현해서 데이터베이스에서 사용자 정보를 조회한 후,
 * Spring Security가 사용하는 UserDetails 객체로 변환한다.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // UserRepository를 주입받아서 데이터베이스에서 사용자 정보를 조회하는 데 사용한다.
    private final UserRepository userRepository;

    /**
     * 생성자 주입을 통해 UserRepository 의존성을 전달받는다.
     *
     * @param userRepository 사용자 정보를 조회하기 위한 Repository
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 입력받은 이메일을 기준으로 사용자를 조회하고 UserDetails 객체로 변환해 반환한다.
     *
     * @param email 사용자 식별자로 사용되는 이메일 값
     * @return Spring Security에서 사용하는 UserDetails 객체
     * @throws UsernameNotFoundException 해당 이메일의 사용자가 존재하지 않으면 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // UserRepository를 통해 이메일에 해당하는 사용자를 조회한다.
        // 만약 사용자가 없으면 UsernameNotFoundException을 발생시킨다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없음: " + email));

        // 조회한 User 객체를 Spring Security가 사용하는 UserDetails 객체로 변환한다.
        // org.springframework.security.core.userdetails.User 클래스는 UserDetails 인터페이스의 기본 구현체다.
        // 인자 설명:
        // - user.getEmail(): 사용자 이름으로 이메일 사용
        // - user.getPassword(): 암호화된 비밀번호
        // - true: 계정이 활성화되었음을 나타냄
        // - true: 계정이 만료되지 않았음을 나타냄
        // - true: 자격 증명이 만료되지 않았음을 나타냄
        // - true: 계정이 잠겨있지 않음을 나타냄
        // - 마지막 인자는 사용자 권한 목록인데, 여기서는 빈 리스트를 전달해 별도 권한 없음 처리
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                true,   // 계정 활성화 여부
                true,   // 계정 만료 여부
                true,   // 자격 증명(비밀번호) 만료 여부
                true,   // 계정 잠금 여부
                java.util.Collections.emptyList()  // 사용자 권한 (현재 빈 리스트)
        );
    }
}
