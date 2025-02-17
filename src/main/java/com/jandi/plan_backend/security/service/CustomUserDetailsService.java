package com.jandi.plan_backend.security.service;

import com.jandi.plan_backend.security.entity.CustomUserDetails;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService는 Spring Security에서 인증 및 권한 정보를 로드할 때 사용하는 서비스 클래스다.
 * 이 클래스는 UserDetailsService 인터페이스를 구현해서 데이터베이스에서 사용자 정보를 조회한 후,
 * CustomUserDetails 객체로 변환하여 반환한다.
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
     * 입력받은 이메일을 기준으로 사용자를 조회하고 CustomUserDetails 객체로 변환해 반환한다.
     *
     * @param email 사용자 식별자로 사용되는 이메일 값
     * @return CustomUserDetails 객체
     * @throws UsernameNotFoundException 해당 이메일의 사용자가 존재하지 않으면 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 사용자 조회 (존재하지 않으면 예외 발생)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없음: " + email));

        // 조회한 User 객체를 CustomUserDetails로 변환하여 반환
        return new CustomUserDetails(user);
    }
}
