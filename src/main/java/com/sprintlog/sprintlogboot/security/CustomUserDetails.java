package com.sprintlog.sprintlogboot.security;

import com.sprintlog.sprintlogboot.domain.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


//Spring Security는 우리의 User엔티티를 모름 UserDetails라는 약식밖에 모름
//우리의 User 정보를 UserDetails라는 양식에 맞춰서 포장한 객체가 CustomUserDetail
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

  private final User user;

  @Override //유저의 권환(Role)을 리턴 //GrantedAuthority 형태로 변환해서 저장, 접두어로 ROLE_ 을 붙여서 저장
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }


  /*
  계정 상태 플래그에 대한 메서드(계정만료 / 계정 잠김 / 비밀번호 만료 / 계정 비활성화)
  true-통과 false-잠김
   */

  @Override
  public boolean isAccountNonExpired() {
    return UserDetails.super.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return UserDetails.super.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return UserDetails.super.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return UserDetails.super.isEnabled();
  }
}
