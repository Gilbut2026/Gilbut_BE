package com.gilbeot.gilbut.security;

import com.gilbeot.gilbut.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 권한 안 쓸 경우 비워두기
    }

    @Override
    public String getPassword() {
        return null; // OAuth라서 비밀번호 없음
    }

    @Override
    public String getUsername() {
        return String.valueOf(user.getId()); // userId를 username처럼 사용
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
