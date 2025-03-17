package com.jandi.plan_backend.security;

import com.jandi.plan_backend.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 사용자 정보를 담은 커스텀 UserDetails 구현체.
 * 추가로 userId를 제공하여, 백엔드 로직에서 사용자 고유 식별자를 사용할 수 있도록 한다.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        // User 엔티티에 PK가 "userId" 필드로 정의되어 있고, getter가 getUserId()라면:
        this.user = user;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRoleEnum().name()));
    }

    public Integer getUserId() {
        return user.getUserId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRoleEnum().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
